import os, sys
import json
import uuid

# * lib
from flask import request, make_response, jsonify, Blueprint, send_from_directory, send_file
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from init.init_db import rdb
from utils.security.check import check_product
import utils.error.custom_error as error

## 경로
ROOT_PATH = os.path.dirname(os.path.abspath(os.path.dirname(__file__)))
UPLOAD_FOLDER = os.path.join(ROOT_PATH, 'upload')
PROFILE_FOLDER = os.path.join(ROOT_PATH, 'profile')

# upload 폴더 유무 확인
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

if not os.path.exists(PROFILE_FOLDER):
    os.makedirs(PROFILE_FOLDER)

# 파일 권한 설정 (chmod 644)
os.chmod(UPLOAD_FOLDER, 0o644)

## Blueprint
bp = Blueprint('imgs', __name__, url_prefix='/imgs')


@bp.route("/profile/<string:username>", methods=["POST"])
def profile_upload(username):
    try:
        accept_type = request.headers['Content-Type']
        acc_len = len('multipart/form-data')
        if len(accept_type) < acc_len or not accept_type[:acc_len] == 'multipart/form-data':
            return jsonify({"message": "Invalid header."}), 400

        file = request.files['imgs']
        if not file:
            return jsonify({"message": "Empty Image"}), 400
        img_id = uuid.uuid4()
        file_path = os.path.join(PROFILE_FOLDER, str(user_id))
        if not os.path.exists(file_path):
            os.makedirs(file_path)
        file_name = f"{img_id}.jpg"

        # 파일 저장
        file_path = os.path.join(file_path, file_name)
        file.save(file_path)

        # 반환할 정보들
        res_info = {
            "file_name": file_name
        }
        # DB 저장
        # rdb.session.add(ProductImageModel(file_name=file_name, product=product_data))

        rdb.session.commit()
        return jsonify(res_info), 200
    except Exception as e:
        print(e)


@bp.route("/upload/<int:product_id>", methods=["POST"])
def upload(product_id):
    if not request.files:
        pass

    # TODO JWT Token
    # check accept-encoding
    accept_type = request.headers.get('Content-Type')
    acc_len = len('multipart/form-data')
    if len(accept_type) < acc_len or not accept_type[:acc_len] == 'multipart/form-data':
        return jsonify({"message": "Invalid header."}), 400

    product_data = ProductModel.query.filter_by(product_id=product_id).first()
    if not product_data:
        raise error.DBNotFound("Product")

    file_path_list = []
    images = request.files.getlist('imgs')
    if not images:
        raise error.EmptyError("Image")

    for image in images:
        img_id = uuid.uuid4()
        file_path = os.path.join(UPLOAD_FOLDER, str(product_id))
        file_name = f"{img_id}.jpg"

        res_path = os.path.join("imgs/download", str(product_id), file_name)
        print(res_path)
        if not os.path.exists(file_path):
            os.makedirs(file_path)

        # 파일 저장
        file_path = os.path.join(file_path, file_name)
        image.save(file_path)

        # 반환할 정보들
        res_info = {
            "path": res_path,
            "product_id": product_id,
            "filename": file_name
        }
        file_path_list.append(res_info)

        # DB 저장
        rdb.session.add(ProductImageModel(file_name=file_name, product=product_data))

    rdb.session.commit()
    return jsonify(file_path_list), 200


# 오직 첫번째로 display한 사진을 가져온다.
# product_id
# type = 0(첫번째 사진만)
@bp.route('/<int:product_id>', methods=["GET"])
def display_image(product_id):
    try:
        display_type = request.args.get('type')
        if not display_type:
            raise error.MissingParams('type')

        product_dir = os.path.join(UPLOAD_FOLDER, str(product_id))
        files = os.listdir(product_dir)
        # Show only first images
        if display_type == "0":
            return send_from_directory(product_dir, files[0])
        elif display_type == "1":
            return send_from_directory(product_dir)
        try:
            num_images = int(display_type)
            if num_images > len(files):
                num_images = len(files)
            images = [send_from_directory(product_dir, filename) for filename in files[:num_images]]
            return Response(response=json.dumps(images), status=200, mimetype='application/json')
        except ValueError:
            raise error.InvalidParams('type')
    except sqlalchemy.exc.OperationalError:
        raise error.DBConnectionError()


@bp.route('/download/<int:product_id>/<path:filename>', methods=['GET'])
def download_file(product_id, filename):
    try:

        # 파일 저장
        file_path = os.path.join(UPLOAD_FOLDER, str(product_id), filename)
        mimetype = 'image/jpeg'

        print(file_path)
        # with open(file_path, 'rb') as f:
        #     contents = f.read()
        return send_file(file_path, mimetype=mimetype)
    except:
        # 파일이 존재하지 않을 경우 예외처리합니다.
        return "File not found", 404
