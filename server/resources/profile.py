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

bp = Blueprint('profile', __name__, url_prefix='/profile')

# * User profile 
# type=0(default = all)
# 개수만큼 사진을 보여줄 수 있다. 
# 사용자 정보와 product를 볼 수 있다. 
@bp.route("/<int:user_id>", methods=["GET"])
@jwt_required()
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

@bp.route("/<int:user_id>", methods=["POST"])
@jwt_required()
def modify_profile(user_id):
    body = request.get_json()
    user = UserModel.query.get(user_id)
    if not user:
        raise error.DBNotFound("User")
    
    if not body['username']:
        raise error.MissingParams('username')
    user.username = body['username']

    return jsonify({"message":"Success"}), 200 