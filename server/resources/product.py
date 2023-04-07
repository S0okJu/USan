import binascii
import io
import os, sys
import json 
import datetime
import uuid
import base64
import gzip

# * lib
from flask import request,Response, jsonify, Blueprint, send_from_directory
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from db.init_db import rdb
import utils.color as msg
from utils.changer import res_msg
import utils.error.custom_error as error 


ROOT_PATH = os.path.dirname(os.path.abspath(os.path.dirname(__file__)))
PROJECT_HOME = '/workspace/firstContainer/USan'
UPLOAD_FOLDER = os.path.join(ROOT_PATH,'upload')
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

bp = Blueprint('product', __name__, url_prefix='/product')

# 상품 정보 조회
# 특정 상품을 메인으로 볼때 사용된다.  
@bp.route('/<int:product_id>', methods=["GET"])
def get_product(product_id):
    try:
        question = ProductModel.query.get(product_id)
        if not question:
            raise error.DBNotFound('Product')
                
        q_dict = {}
        # Model to Json 
        for col in question.__table__.columns:
            q_dict[col.name] = str(getattr(question, col.name))
        author = UserModel.query.get(q_dict['author_id']) # get author name 
        del(q_dict['author_id'])
        q_dict['author'] = author 
        return Response(
            response = json.dumps(q_dict, ensure_ascii=False),
            status=200,
            mimetype="application/json" 
        )
    
    except sqlalchemy.exc.SQLAlchemyError as e:
        msg.error(e)
        raise error.DBConnectionError()

# 상품 조회 (개수별)
# @param page_per 한 페이지당 개수, page = page 인덱스 
# @return 상품명, 사용자, 수정일 
@bp.route('/display', methods=["GET"])
def display_product():

    # TODO 맨 첫 번째 사진 가져오기 
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
            product_json = product.to_dict()
            product_json['modified_date'] = product.modified_date.strftime("%Y-%m-%d %H:%M:%S")
            product_json['img_url'] = product.product_imgs[0].to_dict()['url']
            result_json[product.product_id] = json.dumps(product_json)

            # TODO author는 query 대신 역참조 데이터 사용해보기 
            # product_json['author'] = str(product.author.username)
            
        return Response(
            response = json.dumps(result_json, ensure_ascii=False, indent=3).encode('utf-8'),
            status=200,
            mimetype="application/json"
        )
    except sqlalchemy.exc.OperationalError as e:
        msg.error(e)
        raise error.DBConnectionError()
    
@bp.route('/post',methods=["POST"])
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
            status=False)
        rdb.session.add(product_session)
        
        rdb.session.commit()
        return Response(
            response = json.dumps({"message":"Successfully store the data in DB"}),
            status=200,
            mimetype="application/json"
        )
    
    except sqlalchemy.exc.SQLAlchemyError as e:
        msg.error(e)
        raise error.DBConnectionError()


@bp.route('/modify/<int:product_id>',methods=["POST"])
def modify_product(product_id):
    # TODO User check using JWT Token 
    
    # Modify the data
    body = request.get_json() 
    if not body:
        raise error.Empty('JSON')

    obj = json.loads(json.dumps(body))
    p = ProductModel.query.get(product_id)
    if not p:
        raise error.DBNotFound('Product')
    
    # fix title, content, address, price
    if obj['title'] != p.title:
        p.title = obj['title']
    elif obj['content'] != p.content:
        p.content = obj['content']
    elif obj['address'] != p.address:
        p.address = obj['address']
    elif obj['price'] != p.price:
        p.price = obj['price']
    
    p.modified_date = datetime.datetime.now()
    rdb.session.commit()
    return {"status_code" : 200, "message":"Modify product completely!"}

@bp.route('/delete/<int:product_id>',methods=["POST"])
def delete(product_id):
    # TODO User check using JWT Token 
    
    p = ProductModel.query.get(product_id)
    if not p:
        raise error.DBNotFound('Product')
    rdb.session.delete(p)
    rdb.session.commit()

    return {"status_code" : 200, "message":"Delete product completely!"}


# * User profile 
@bp.route("/user_profile/<int:user_id>", methods=["GET"])
def user_profile(user_id):
    user = UserModel.query.get(user_id)
    if not user:
        raise error.DBNotFound("User")
    products = ProductModel.query.filter_by(author_id=user_id).all()
    result = {"user_info": str(user), "products": [str(p) for p in products]}
    