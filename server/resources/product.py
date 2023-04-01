import binascii
import io
import os, sys
import json 
import datetime
import uuid
import base64

# * lib
from flask import request,Response, jsonify, Blueprint
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from db.init_db import rdb
import utils.color as msg
from utils.changer import res_msg

PROJECT_HOME = '/workspace/firstContainer/USan'
UPLOAD_FOLDER = '{}/uploads/'.format(PROJECT_HOME)

bp = Blueprint('product', __name__, url_prefix='/product')

# 상품 정보 조회
# 개인 상품을 메인으로 볼때 사용된다.  
@bp.route('/<int:product_id>', methods=["GET"])
def get_product(product_id):
    try:
        question = ProductModel.query.get(product_id)
        if not question:
            msg.error("Data is not found!")
            return Response(
                response = json.dumps({"message":"No data in DB"}),
                status=404,
                mimetype="application/json"
            )
                
        q_dict = {}
        for col in question.__table__.columns:
            q_dict[col.name] = str(getattr(question, col.name))
        author = UserModel.query.get(q_dict['author_id'])
        del(q_dict['author_id'])
        q_dict['author'] = author
        return jsonify(q_dict)
    
    except sqlalchemy.exc.SQLAlchemyError as e:
        msg.error(e)
        return Response(
            response = json.dumps({"message":"Database Error"}),
            status=503,
            mimetype="application/json"
        )

# 상품 조회 (개수별)
# get num, page 
@bp.route('/display', methods=["GET"])
def display_product():
    # 상품명, 제작자, 생성일 만 표시 
    page_per = int(request.args.get('page_per'))
    page = int(request.args.get('page'))
    
    if not page_per or not page:
        return Response(
            response = json.dumps({"message":"Empty parameters."}),
            status=400,
            mimetype="application/json"
        )

    try:
        products = ProductModel.query.order_by(ProductModel.modified_date.desc()).paginate(page= page, per_page = page_per)
        result_json = dict()
        for product in products.items:
            product_json = dict()
            product_json['title'] = product.title
            # author는 query 대신 역참조 데이터 사용해보기 
            product_json['author'] = UserModel.query.get(product.author_id).username
            product_json['modified_date'] = product.modified_date.strftime("%Y-%m-%dT%H:%M:%S")
            
            result_json[product.product_id] = json.dumps(product_json)
            print(result_json)
            
        return Response(
            response = json.dumps(result_json, ensure_ascii=False, indent=3).encode('utf-8'),
            status=200,
            mimetype="application/json"
        )
    except sqlalchemy.exc.SQLAlchemyError as e:
        msg.error(e)
        return Response(
            json.dumps({"message":"Database Error"}),
            status=503,
            mimetype="application/json"
        )
    
@bp.route('/post',methods=["POST"])
def post_product():
    
    try:
        # TODO User check using JWT Token 
        body = request.get_json() 
        if not body:
            return res_msg(400, "Must provide products options.")
        
        obj = json.loads(json.dumps(body))
        print(obj)
        author_data = UserModel.query.filter(UserModel.username == obj['author']).first()
        if not obj:
            msg.error("Data is not found!")
            return res_msg(404,"No data in DB")
        
        product_session = ProductModel(title=obj['title'], author=author_data,
            price=int(obj['price']),address=obj['address'], content=obj['content'],
            created_date= datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S"), modified_date=datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
            status=False)
        rdb.session.add(product_session)
        rdb.session.commit()
        
        # TODO Image Download 
        
        return {"status_code" : 200, "message":"Post product completely!"}
    except sqlalchemy.exc.SQLAlchemyError as e:
        msg.error(e)
        return res_msg(503, "Database Error")



@bp.route('/modify/<int:product_id>',methods=["POST"])
def modify_product(product_id):
    # TODO User check using JWT Token 
    
    # Modify the data
    body = request.get_json() 
    if not body:
        msg.error("Data is not found!")
        return res_msg(404,"No data in DB")

    obj = json.loads(json.dumps(body))
    p = ProductModel.query.get(product_id)
    
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

@bp.route('/delete/<int:product_id>',methods=["GET"])
def delete(product_id):
    # TODO User check using JWT Token 
    
    p = ProductModel.query.get(product_id)
    if not p:
        msg.error("Data is not found!")
        return res_msg(404,"No data in DB")
    rdb.session.delete(p)
    rdb.session.commit()

    return {"status_code" : 200, "message":"Delete product completely!"}


# 이건 클라이언트측과 상의해야할 것 가탇. 
@bp.route('/images',methods=["GET"])
def upload_imgs():
    if request.method == 'POST':
        upload_imgs = request.files
        
    pass 