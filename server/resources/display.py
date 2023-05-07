import os, sys
import json

# * lib
from flask import request,Response, jsonify, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from init.init_db import rdb
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
    return jsonify(result), 200

@bp.route("/<int:user_id>/productlist", methods=["GET"])
def get_user_productlist(user_id):
    page_per = int(request.args.get('page_per'))
    page = int(request.args.get('page'))
    
    if not page_per:
        raise error.InvalidParams()
    if not page:
        raise error.InvalidParams()

    result_json = list()
    try:
        products = ProductModel.query.filter(ProductModel.author_id == int(user_id)).order_by(ProductModel.modified_date.desc()).paginate(page= page, per_page = page_per)
        if not products:
            raise error.DBNotFound('Product')
        for product in products.items:
            product_json = dict()
            product_json['title'] = product.title
            product_json['price'] = int(product.price)
            product_json['status'] = product.status
            if product.product_imgs:
                product_json['img'] = product.product_imgs[0].to_dict()['file_name']
            else:
                product_json['img']  = None 
            result_json.append(product_json)
                
        return jsonify(result_json), 200
    except sqlalchemy.exc.OperationalError:
        raise error.DBConnectionError()

# 상품 조회 (개수별)
# @param page_per 한 페이지당 개수, page = page 인덱스 
# @return 상품명, 사용자, 수정일 
@bp.route("/productlist", methods=["GET"])
# @jwt_required()
def get_productlist():

    page_per = int(request.args.get('page_per'))
    page = int(request.args.get('page'))
    list_type = int(request.args.get('type'))

    # Initial 
    # if not page_per:
    #     page_per = 4
    # if not page:
    #     page = 1 
    # if list_type is None:
    #     list_type = 0

    try:
        result_json = list()
        if list_type == 0:
            products = ProductModel.query.order_by(ProductModel.modified_date.desc()).paginate(page= page, per_page = page_per)
            for product in products.items:
                product_json = dict()
                product_json['product_id'] = product.product_id
                product_json['title'] = product.title
                product_json['author'] = product.author.username if product.author else None
                product_json['modified_date'] = product.modified_date.strftime("%Y-%m-%d %H:%M:%S") 
                product_json['favorite'] = product.favorite
                product_json['status'] = product.status
                product_json['price'] = product.price
                if product.product_imgs:
                    product_json['img'] = product.product_imgs[0].to_dict()['file_name']
                else:
                    product_json['img'] = None
                result_json.append(product_json)
            return jsonify(result_json), 200 
        elif list_type == 1:
            user_id = get_jwt_identity()
            products = ProductModel.query.filter(ProductModel.author_id == int(user_id)).order_by(ProductModel.modified_date.desc()).paginate(page= page, per_page = page_per)
            for product in products.items:
                product_json = dict()
                product_json['title'] = product.title
                product_json['price'] = int(product.price)
                product_json['status'] = bool(product.status)
                if product.product_imgs:
                    product_json['img'] = product.product_imgs[0].to_dict()['file_name']
                else:
                    product_json['img']  = None 
                result_json.append(product_json)

            return jsonify(result_json), 200 

        else:
            raise error.InvalidParams()
        
    except sqlalchemy.exc.OperationalError as e:

        raise error.DBConnectionError()