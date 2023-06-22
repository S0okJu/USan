import os, sys
import json
import datetime
import re

# * lib
from flask import request, Response, jsonify, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask import send_file
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel, FavoriteModel
from init.init_db import rdb
import utils.color as msg
import utils.error.custom_error as error

bp = Blueprint('product', __name__, url_prefix='/product')


def extract_numbers(s):
    return "".join(re.findall(r'\d+', s))


# 상품 정보 조회
# 특정 상품을 메인으로 볼때 사용된다.
@bp.route('/<int:product_id>', methods=["GET"])
@jwt_required()
def get_product(product_id):
    check_type = int(request.args.get('type'))
    if check_type != 0 and check_type != 1:
        raise error.InvalidParams()

    try:
        question = ProductModel.query.get(product_id)
        user_id = get_jwt_identity()
        if not question:
            raise error.DBNotFound('Product')

        q_dict = question.to_dict()

        author = question.author.to_dict()['username']
        res_dict = {}

        res_dict['title'] = q_dict['title']
        res_dict['author'] = author
        res_dict['content'] = q_dict['content']
        res_dict['price'] = q_dict['price']
        if check_type == 0:
            res_dict['address'] = question.address
            res_dict['status'] = q_dict['status']
            res_dict['modified_date'] = q_dict['modified_date']
            fav = FavoriteModel.query.filter_by(product_id=int(product_id), user_id=int(user_id)).first()
            if fav:
                res_dict['favorite'] = fav.favorite
            else:
                res_dict['favorite'] = False

            related_product = ProductModel.query.filter(ProductModel.author_id == int(user_id),
                                                        ProductModel.product_id != int(product_id)).order_by(
                ProductModel.modified_date.desc()).limit(2).all()
            realted_list = []
            for related in related_product:
                rproduct = dict()
                rproduct['product_id'] = related.product_id
                rproduct['title'] = related.title
                rproduct['price'] = related.price
                realted_list.append(rproduct)

            res_dict['related'] = realted_list
        elif check_type == 1:
            print("address" + question.address)

            res_dict['address'] = q_dict['address']

        # print(res_dict)
        return jsonify(res_dict), 200

    except sqlalchemy.exc.SQLAlchemyError as e:
        raise error.DBConnectionError()


@bp.route('/post', methods=["POST"])
@jwt_required()
def post_product():
    try:
        # TODO User check using JWT Token
        body = request.get_json()
        print(f"product post : {body}")
        if not body:
            raise error.Empty('Json')

        obj = json.loads(json.dumps(body))
        author_data = UserModel.query.filter(UserModel.username == obj['author']).first()
        if not author_data:
            raise error.DBNotFound('User')

        product_session = ProductModel(title=obj['title'], author=author_data,
                                       price=int(obj['price']), address=obj['address']['name'],
                                       latitude=obj['address']['latitude'], longitude=obj['address']['longitude'],
                                       content=obj['content'],
                                       created_date=datetime.datetime.now(), modified_date=datetime.datetime.now(),
                                       status=False)
        fav_session = FavoriteModel(user_id=author_data.user_id, product=product_session, favorite=False)

        rdb.session.add(fav_session)
        rdb.session.add(product_session)
        rdb.session.commit()
        return jsonify({"status_code": 200, "message": "Success"}), 200

    except sqlalchemy.exc.SQLAlchemyError as e:
        print(e)
        raise error.DBConnectionError()


@bp.route('/modify', methods=["POST"])
@jwt_required()
def modify_product():
    # TODO User check using JWT Token

    # Modify the data
    body = request.get_json()
    if not body:
        raise error.Empty('JSON')

    obj = json.loads(json.dumps(body))
    product_id = obj['product_id']
    p = ProductModel.query.get(product_id)
    if not p:
        raise error.DBNotFound('Product')

    # 게시글 작성자와 현재 사용자 일치 여부 확인
    current_user_id = get_jwt_identity()
    if p.author_id != current_user_id:
        return jsonify({'error': '게시글 작성자만 수정할 수 있습니다.'}), 403

    # Fix title, content, address, price
    if 'title' in obj:
        p.title = obj['title']
    if 'content' in obj:
        p.content = obj['content']
    if 'address' in obj:
        p.address = obj['address']['name']
        p.latitude = float(obj['address']['latitude'])
        p.longitude = float(obj['address']['longitude'])
    if 'price' in obj:
        p.price = obj['price']

    p.modified_date = datetime.datetime.now()

    # 이미지 파일 이름 반환
    image_filenames = [img.file_name for img in p.product_imgs]
    image_urls = [f"https://13.124.86.136:55328/images/{filename}" for filename in image_filenames]
    rdb.session.commit()

    return jsonify({
        "status_code": 200,
        "message": "상품 수정이 완료되었습니다.",
        "image_url": image_url
    })


@bp.route('/delete/<int:product_id>', methods=["GET"])
@jwt_required()
def delete(product_id):
    user_id = get_jwt_identity()
    print(f"user_id : {user_id}, product_id : {product_id}")
    p = ProductModel.query.filter_by(author_id=int(user_id), product_id=int(product_id)).first()
    if not p:
        raise error.DBNotFound('Product')

    favorites = FavoriteModel.query.filter_by(product_id=product_id).all()
    for favorite in favorites:
        rdb.session.delete(favorite)

    rdb.session.delete(p)
    rdb.session.commit()

    return jsonify({"status_code": 200, "message": "Success"}), 200


@bp.route("/favorite", methods=["GET"])
@jwt_required()
def check_favorite():
    product_id = int(request.args.get('product_id'))
    check_type = int(request.args.get('type'))
    user_id = get_jwt_identity()

    product = FavoriteModel.query.filter_by(user_id=int(user_id), product_id=int(product_id))
    if product:
        fav = FavoriteModel.query.filter_by(product_id=product_id, user_id=user_id).first()
        if not fav:
            if check_type == 1:
                f = FavoriteModel(user_id=user_id, product_id=product_id, favorite=True,
                                  created_date=datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                                  modified_date=datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
                rdb.session.add(f)
        else:
            if check_type == 0:
                fav.favorite = False
                fav.modified_date = datetime.datetime.now()
            elif check_type == 1:
                fav.favorite = True
                fav.modified_date = datetime.datetime.now()
            else:
                error.InvalidParams()
        rdb.session.commit()

    else:
        raise error.DBNotFound('Product')
    return jsonify({"status_code": 200, "message": "Success"}), 200


@bp.route('/status', methods=["GET"])
@jwt_required()
def check_status():
    product_id = request.args.get('product_id')
    check_type = request.args.get('type')

    product = ProductModel.query.get(product_id)
    if product:
        if check_type == 0:
            product.status = False
        elif check_type == 1:
            product.status = True
        else:
            error.InvalidParams()
        rdb.session.commit()
    else:
        raise error.DBNotFound('Product')
    return jsonify({"status_code": 200, "message": "Success"}), 200


# 목적지 위치 정보를 얻는 함수
@bp.route('/dest/<string:chat_id>', methods=["GET"])
@jwt_required()
def get_dest(chat_id):
    product_id = extract_numbers(chat_id)
    product = ProductModel.query.get(product_id)

    if product is None:
        print("None")
        return {"error": "Product not found"}, 404

    res = {
        "lat": product.latitude,
        "lng": product.longitude
    }
    print(res)
    return res


@bp.route('/check/<string:username>/<string:chat_id>', methods=["GET"])
@jwt_required()
def get_role(username, chat_id):
    product_id = extract_numbers(chat_id)

    product = ProductModel.query.get(int(product_id))
    print(product.author.username)
    if product.author.username == username:
        print("role is 0")
        return jsonify({'role': 0}), 200
    else:
        print("role is 1")
        return jsonify({'role': 1}), 200


@bp.route('/image/<path:image_path>')
def get_image(image_path):
    # 이미지 파일의 경로를 설정
    image_file = os.path.join('path/to/images', image_path)

    # 이미지 파일을 읽어서 반환
    return send_file(image_file, mimetype='image/jpeg')


@bp.route("/url/<int:product_id>", methods=["GET"])
def get_url(product_id):
    num_check = int(request.args.get('num'))

    url = f"/download/{str(product_id)}/"
    product = ProductModel.query.get(product_id)

    url_list = list()
    if product is None:
        return jsonify({'msg': 'Product not found'}), 404
    product = product.to_dict()
    if not product['images']:
        return jsonify({'msg': 'empty'}), 200
    for i in product['images']:
        url_list.append(url + i)
    print(url_list)
    if num_check == 0:
        return jsonify({'imgs': url_list}), 200
    else:
        return jsonify({'imgs': url_list[0:num_check]}), 200
