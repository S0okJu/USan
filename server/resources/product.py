import io
import os, sys
import json 
import datetime
import uuid
import base64
from PIL import Image

# * lib
from flask import request,Response, jsonify, Blueprint
import sqlalchemy.exc 
from werkzeug.utils import secure_filename

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from db.init_db import rdb
import utils.color as msg
from utils.changer import res_msg, model2json

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
            return res_msg(404,"No data in DB")
                
        q_dict = {}
        for col in question.__table__.columns:
            q_dict[col.name] = str(getattr(question, col.name))
        author = UserModel.query.get(q_dict['author_id'])
        del(q_dict['author_id'])
        q_dict['author'] = author
        return jsonify(q_dict)
    
    except sqlalchemy.exc.SQLAlchemyError as e:
        msg.error(e)
        return res_msg(503, "Database Error")

# 상품 조회 (개수별)
# 이건 고민을 좀 더 해봐야할 것 같다.. 
@bp.route('/display', methods=["GET"])
def display_product():
    # 상품명, 제작자, 생성일 만 표시 
    pass
  


@bp.route('/post',methods=["POST"])
def post_product():
    
    try:
        # TODO User check using JWT Token 
        body = request.get_json() 
        if not body:
            return res_msg(400, "Must provide message.")
        
        obj = json.loads(json.dumps(body))
        
        author_data = UserModel.query.filter(UserModel.username == obj['author']).first()
        if not obj:
            msg.error("Data is not found!")
            return res_msg(404,"No data in DB")
        
        product_session = ProductModel(title=obj['title'], author=author_data,
            price=int(obj['price']),address=obj['address'], content=obj['content'],
            created_date= datetime.datetime.now(), modified_date=datetime.datetime.now(),
            status=False)
        rdb.session.add(product_session)
        rdb.session.commit()
        
        # 이미지 저장 및 DB 저장 
        img = obj['imgs']
        img_bytes =base64.b64decode(img)
        filename = str(uuid.uuid4())
        total_path = UPLOAD_FOLDER+filename+".png"
        
        # create new file
        with open(total_path,'w'):
            pass
        
        with Image.open(io.BytesIO(img_bytes)) as im:
            im.save(total_path,"PNG")
        
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


        
        
        
        
