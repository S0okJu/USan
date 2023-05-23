import os, sys
import json
import uuid 

# * lib
from flask import request,Response, jsonify, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel, UserProfileModel
from init.init_db import rdb
import utils.color as msg
from utils.changer import res_msg
import utils.error.custom_error as error

bp = Blueprint('profile', __name__, url_prefix='/profile')

## 경로 
ROOT_PATH = os.path.dirname(os.path.abspath(os.path.dirname(__file__)))
PROFILE_FOLDER = os.path.join(ROOT_PATH, 'profile')

# * User profile 정보 가져오기 
# type=0(default = all)
# 개수만큼 사진을 보여줄 수 있다. 
# 사용자 정보와 product를 볼 수 있다. 
@bp.route("/<string:username>", methods=["GET"])
@jwt_required()
def user_profile(username):
    user = UserModel.query.filter_by(username=username).first()
    if not user:
        raise error.DBNotFound("User")

    profile_session = UserProfileModel.query.filter_by(user=user).first()
    if not profile_session:
        result ={
            "username": username,
            "profile": None
        }
    else:
        result ={
            "username" : username,
            "profile" : profile_session.filename
        }  

    return jsonify(result), 200

# 프로필 이미지 업로드 
@bp.route("/<string:username>/upload", methods=["POST"])
@jwt_required()
def upload_profile(username):
    # user_id = get_jwt_identity()
    if not request.files:
        pass
    
    accept_type = request.headers['Content-Type']
    acc_len = len('multipart/form-data')
    if len(accept_type) < acc_len or not accept_type[:acc_len] == 'multipart/form-data':
        return jsonify({"message":"Invalid header."}), 400

    user = UserModel.query.filter_by(username=username).first()
    if not user:
        raise error.DBNotFound("User")
    
    image = request.files['img']
    if not image:
        raise error.EmptyError("Image")
    
    img_id = uuid.uuid4() 
    file_path = os.path.join(PROFILE_FOLDER,str(user.user_id))
    file_name = f"{img_id}.jpg"
        
    if not os.path.exists(file_path):
        os.makedirs(file_path)
        
    # 파일 저장 
    file_path = os.path.join(file_path, file_name)
    image.save(file_path)
        
    # 반환할 정보들 
    res_info = {
        "filename":file_name
    }

    profile_s = UserProfileModel.query.filter_by(user=user)
    if profile_s:
        profile_s.filename = file_name
    else:
    # DB 저장 
        rdb.session.add(UserProfileModel(user=user, filename=file_name))
    rdb.session.commit()
    return jsonify(res_info), 200 

@bp.route("/<string:username>/modify", methods=["POST"])
@jwt_required()
def modify_profile(username):
    
    body = request.get_json()
    if not body:
        raise error.EmptyJSONError()
        
    user = UserModel.query.filter_by(username=username).first()
    if not user:
        raise error.DBNotFound("User")
    
    if not body['username']:
        raise error.MissingParams('username')
    user.username = body['username']
    rdb.session.commit()
    return jsonify({"message":"Success"}), 200 