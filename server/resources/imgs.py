import os, sys
import json 
import uuid

# * lib
from flask import request,make_response, jsonify, Blueprint, send_from_directory,send_file
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from init.init_db import rdb
from utils.security.check import check_product
import utils.error.custom_error as error 

## 경로 
ROOT_PATH = os.path.dirname(os.path.abspath(os.path.dirname(__file__)))
UPLOAD_FOLDER = os.path.join(ROOT_PATH,'upload')
# upload 파일이 있는지 확인 
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

## Blueprint
bp = Blueprint('imgs', __name__, url_prefix='/imgs')

@bp.route("/upload/<int:product_id>", methods=["POST"])
def upload(product_id):

    if not request.files:
        pass

    
    # TODO JWT Token  
    #check accept-encoding 
    accept_type = request.headers['Content-Type']
    acc_len = len('multipart/form-data')
    if len(accept_type) < acc_len or not accept_type[:acc_len] == 'multipart/form-data':
        
        return jsonify({"message":"Invalid header."}), 400

        
    product_data =  ProductModel.query.filter_by(product_id=product_id).first()
    if not product_data:
        raise error.DBNotFound("Product")
    
    file_path_list = list()
    images = request.files.getlist('imgs')
    if not images:
        raise error.EmptyError("Image")
    
    for image in images:
        img_id = uuid.uuid4() 
        file_path = os.path.join(UPLOAD_FOLDER,str(product_id))
        file_name = f"{img_id}.jpg"
        
        if not os.path.exists(file_path):
            os.makedirs(file_path)
        
        # 파일 저장 
        file_path = os.path.join(file_path, file_name)
        image.save(file_path)
        
        # 반환할 정보들 
        res_info = {
            "file_name":file_name
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

        product_dir = os.path.join(UPLOAD_FOLDER,str(product_id))
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


@bp.route('/download/<int:product_id>/<string:filename>', methods=['GET'])
def download_file(product_id,filename):
    try:
        
        # 파일 저장 
        file_path = os.path.join(UPLOAD_FOLDER, str(product_id))
        file_path = os.path.join(file_path,filename)
        print(file_path)
        # with open(file_path, 'rb') as f:
        #     contents = f.read()
        return send_file(file_path)
    except:
        # 파일이 존재하지 않을 경우 예외처리합니다.
        return "File not found", 404