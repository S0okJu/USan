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
import utils.error.custom_error as error


bp = Blueprint('location', __name__, url_prefix='/location')

# 위치 정보 수집 후 Redis에 저장 
@bp.route("/<string:username>", methods=["POST"])
def post_location(username):

    user = UserModel.check_by_username(username)
    if user == False:
        raise error.DBNotFound("User")
    
    body = request.get_json()
    if not body:
        raise error.EmptyJSONError()

    # Set the key
    if int(body['role']) == 0:
        role = "seller"
    elif int(body['role']== 1):
         role = "buyer"
    else:
        raise error.OutOfBound()
