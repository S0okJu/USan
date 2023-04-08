import os, sys
import json

# * lib
from flask import request,Response, jsonify, Blueprint
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from db.init_db import rdb
import utils.color as msg
from utils.changer import res_msg
import utils.error.custom_error as error
from utils.security.check import check_product, check_user 

bp = Blueprint('display', __name__, url_prefix='/display')

# * User profile 
# type=0(default = all)
# 개수만큼 사진을 보여줄 수 있다. 
# 사용자 정보와 product를 볼 수 있다. 
@bp.route("/profile/<int:user_id>", methods=["GET"])
def user_profile(user_id):
    user = UserModel.query.get(user_id)
    if not user:
        raise error.DBNotFound("User")
    
    display_type = request.args.get("type")
    display_type = int(display_type,10)
    if not display_type:
        raise error.MissingParams('type')
    
    # 업로드할 수 있는 사진은 5장으로 제한되어 있다. 
    if display_type > 5:
        raise error.OutOfBound()
    
    if display_type == 0:
        products = ProductModel.query.filter_by(author_id=user_id).all()
    else:
        products = ProductModel.query.filter_by(author_id= user_id).limit(display_type)
    result = {"user_info": str(user), "products": [str(p) for p in products]}
    return Response(
        response = jsonify(result),
        status=200,
        mimetype="application/json"
    )


# 상품 조회 (개수별)
# @param page_per 한 페이지당 개수, page = page 인덱스 
# @return 상품명, 사용자, 수정일 
@bp.route('/productlist', methods=["GET"])
def display_product():

    page_per = int(request.args.get('page_per'))
    page = int(request.args.get('page'))
    
    if not page_per:
        raise error.EmptyParams('page_per')
    if not page:
        raise error.EmptyParams('page')

    try:
        products = ProductModel.query.order_by(ProductModel.modified_date.desc()).paginate(page= page, per_page = page_per)
        result_json = dict()
        for product in products.items:
            product_json = dict()
            product_json['title'] = product.title
            product_json['author'] = product.author.username if product.author else None
            product_json['modified_date'] = product.modified_date.strftime("%Y-%m-%d %H:%M:%S")
            if product.product_imgs:
                product_json['img_url'] = product.product_imgs[0].to_dict()['url']
            result_json[product.product_id] = json.dumps(product_json)

        return Response(
            response = jsonify(result_json),
            status=200,
            mimetype="application/json"
        )
    except sqlalchemy.exc.OperationalError as e:
        msg.error(e)
        raise error.DBConnectionError()