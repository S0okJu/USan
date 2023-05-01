import os, sys
import json 
import datetime

# * lib
from flask import request,Response, jsonify, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from init.init_db import rdb
import utils.color as msg
import utils.error.custom_error as error

bp = Blueprint('product', __name__, url_prefix='/product')

# 상품 정보 조회
# 특정 상품을 메인으로 볼때 사용된다.  
@bp.route('/<int:product_id>', methods=["GET"])
# # # @jwt_required()
def get_product(product_id):
    try:
        question = ProductModel.query.get(product_id)
        if not question:
            raise error.DBNotFound('Product')
                
        q_dict = question.to_dict()

        author = question.author.to_dict()['username']
        res_dict ={}
        res_dict['title'] = q_dict['title']
        res_dict['author'] = author
        res_dict['content'] = q_dict['content']
        res_dict['price'] = q_dict['price']
        res_dict['favorite'] = q_dict['favorite']
        res_dict['status'] = q_dict['status']
        res_dict['modified_date'] = q_dict['modified_date']
        return jsonify(res_dict),200
    
    except sqlalchemy.exc.SQLAlchemyError as e:
        raise error.DBConnectionError()
    
@bp.route('/post',methods=["POST"])
# # @jwt_required()
def post_product():
    try:
        # TODO User check using JWT Token 
        body = request.get_json() 
        if not body:
            raise error.Empty('Json')
        
        obj = json.loads(json.dumps(body))
        author_data = UserModel.query.filter(UserModel.username == obj['author']).first()
        if not author_data:
            raise error.DBNotFound('User')
                
        product_session = ProductModel(title=obj['title'], author=author_data,
            price=int(obj['price']),address=obj['address'], content=obj['content'],
            created_date= datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"), modified_date=datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            status=False,favorite=False)
        rdb.session.add(product_session)
        rdb.session.commit()
        return jsonify({"status_code" : 200, "message":"Success"}), 200
    
    except sqlalchemy.exc.SQLAlchemyError as e:
        raise error.DBConnectionError()


@bp.route('/modify',methods=["POST"])
# # @jwt_required()
def modify_product():
    # TODO User check using JWT Token 
    
    # Modify the data
    body = request.get_json() 
    if not body:
        raise error.Empty('JSON')

    obj = json.loads(json.dumps(body))
    p = ProductModel.query.get(obj['product_id'])
    if not p:
        raise error.DBNotFound('Product')

    
    # 게시글 작성자와 현재 사용자 일치 여부 확인
    current_user_id = get_jwt_identity()
    if p.author_id != current_user_id:
        return jsonify({'error': '게시글 작성자만 수정할 수 있습니다.'}), 403


    # fix title, content, address, price
    p.title = obj['title']
    p.content = obj['content']
    p.address = obj['address']
    p.price = obj['price']
    
    p.modified_date = datetime.datetime.now()
    rdb.session.commit()
    return jsonify({"status_code" : 200, "message":"Modify product completely!"})

@bp.route('/delete/<int:product_id>',methods=["GET"])
# # # @jwt_required()
def delete(product_id):
    # TODO User check using JWT Token 
    
    p = ProductModel.query.get(product_id)
    if not p:
        raise error.DBNotFound('Product')
    rdb.session.delete(p)
    rdb.session.commit()

    return jsonify({"status_code" : 200, "message":"Success"})

@bp.route("/favorite",methods=["GET"])
# # # @jwt_required()
def check_favorite():
    product_id = request.args.get('product_id')
    check_type = request.args.get('type')
    
    product = ProductModel.query.get(product_id)
    if product:
        if check_type == 0:
            product.favorite = False
        elif check_type == 1:
            product.favorite = True
        else:
            error.InvalidParams()
        rdb.session.commit()
    else:
        raise error.DBNotFound('Product')
    return jsonify({"status_code" : 200, "message":"Success"}), 200 

@bp.route('/status', methods=["GET"])
# # @jwt_required()
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
    return jsonify({"status_code" : 200, "message":"Success"}), 200 