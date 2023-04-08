import os, sys
import json 
import uuid

# * lib
from flask import request,Response, jsonify, Blueprint, send_from_directory
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from db.init_db import rdb
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

    img_id = uuid.uuid4() # 랜덤 파일명을 제공하기 위해서 사용됨. 
    
    # TODO JWT Token  
    #check accept-encoding 
    accept_type = request.headers['Content-Type']
    acc_len = len('multipart/form-data')
    if len(accept_type) < acc_len or not accept_type[:acc_len] == 'multipart/form-data':
        return Response(
            response = json.dumps({"message":"Invalid header."}),
            status=400,
            mimetype="application/json"
        )
        
    product_data =  ProductModel.query.filter_by(product_id=product_id).first()
    if product_data:
        raise error.DBNotFound("Product")
    
    file_path_list = list()
    images = request.files.getlist('imgs')
    if not images:
        raise error.EmptyError("Image")
    
    for image in images:
        file_path = os.path.join(UPLOAD_FOLDER,str(product_id))
        file_name = f"{img_id}.jpg"
        
        if not os.path.exists(file_path):
            os.makedirs(file_path)
        
        # 파일 저장 
        file_path = os.path.join(file_path, file_name)
        image.save(file_path)
        
        # 반환할 정보들 
        res_info = {
            "file_name":file_name,
            "file_path":file_path
        }
        file_path_list.append(res_info)
        
        # DB 저장 
        rdb.session.add(ProductImageModel(url=file_name, product=product_data))
        
    rdb.session.commit()
    return Response(
        response = jsonify(file_path_list),
        status=200,
        mimetype="application/json"
    )

# 오직 첫번째로 display한 사진을 가져온다. 
# product_id 
# type = 0(첫번째 사진만)
@bp.route('/display', methods=["GET"])
def display_image():
    try:
        product_id = request.args.get('product_id')
        if not product_id:
            raise error.MissingParams('product_id')
    
        if not check_product(product_id):
            raise error.DBNotFound('Product')
    
        display_type = request.args.get('type')
        if not display_type:
            raise error.MissingParams('type')
    
        # Show only first images
        if display_type == "0":
            product_dir = os.path.join(UPLOAD_FOLDER,str(product_id))
            files = os.listdir(product_dir)
            return send_from_directory(product_dir,files[0])

        return json.dumps({"msg":"Hi"})
    except sqlalchemy.exc.OperationalError:
        raise error.DBConnectionError()

@bp.route('/delete', methods=["POST"])
def delete_image():
    pass 