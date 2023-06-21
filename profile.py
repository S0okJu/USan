import os, sys
import json
import uuid

# * lib
from flask import request, Response, jsonify, Blueprint
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
        result = {
            "username": username,
            "profile": None
        }
    else:
        result = {
            "username": username,
            "profile": profile_session.filename
        }

    return jsonify(result), 200


# 프로필 이미지 업로드
@bp.route("/<string:username>/upload", methods=["POST"])
@jwt_required()
def upload_profile(username):
    if 'img' not in request.files:
        return jsonify({"message": "No image file found"}), 400

    image = request.files['img']
    if image.filename == '':
        print('no image selected')
        return jsonify({"message": "No image file selected"}), 400

    user = UserModel.query.filter_by(username=username).first()
    if not user:
        print('no user')
        return jsonify({"message": "User not found"}), 404

    img_id = str(uuid.uuid4())
    file_path = os.path.join(PROFILE_FOLDER, str(user.user_id))
    file_name = f"{img_id}.jpg"

    if not os.path.exists(file_path):
        os.makedirs(file_path)

    file_path = os.path.join(file_path, file_name)
    image.save(file_path)

    res_info = {
        "filename": file_name
    }

    profile_s = UserProfileModel.query.filter_by(user=user).first()
    if profile_s:
        profile_s.filename = file_name
    else:
        rdb.session.add(UserProfileModel(user=user, filename=file_name))
    rdb.session.commit()

    return jsonify(res_info), 200


@bp.route("/<string:username>/modify", methods=["POST"])
@jwt_required()
def modify_profile(username):
    body = request.get_json()
    print(body)
    if not body:
        raise error.EmptyJSONError()

    user = UserModel.query.filter_by(username=username).first()
    if not user:
        raise error.DBNotFound("User")

    user.username = body['username']
    rdb.session.commit()
    return jsonify({"message": "Success"}), 200


@bp.route("/<string:username>/download", methods=["GET"])
@jwt_required()
def download_profile(username):
    user = UserModel.query.filter_by(username=username).first()
    if not user:
        return jsonify({"message": "User not found"}), 404

    profile_session = UserProfileModel.query.filter_by(user=user).first()
    if not profile_session:
        return jsonify({"message": "Profile Image not found"}), 404

    profile_folder_path = os.path.join(PROFILE_FOLDER, str(user.user_id))
    file_path = os.path.join(profile_folder_path, profile_session.filename)

    if not os.path.exists(file_path):
        return jsonify({"message": "File not found"}), 404

    with open(file_path, 'rb') as file:
        response = Response(file.read(), mimetype='image/jpeg')
        response.headers['Content-Disposition'] = f'attachment; filename={profile_session.filename}'
        return response, 200