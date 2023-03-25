import os, sys
import json 
import datetime

from flask import request,Response, jsonify, Blueprint


sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel
from db.init_db import rdb

bp = Blueprint('product', __name__, url_prefix='/product')

# 상품 정보 조회 
@bp.route('/<int:product_id>', methods=["GET"])
def get_product(product_id):
    question = ProductModel.query.get(product_id)
    # Model Date -> Json 
    q_dict = {}
    for col in question.__table__.columns:
        q_dict[col.name] = str(getattr(question, col.name))
    return jsonify(q_dict)

@bp.route('/post',methods=["POST"])
def post_product():
        body = request.get_json() 
        obj = json.loads(json.dumps(body))
        author_data = UserModel.query.filter(UserModel.username == obj['author']).first()
        
        p = ProductModel(title=obj['title'], author=author_data,
            price=obj['price'],address=obj['address'], content=obj['content'],
            created_date= datetime.datetime.now(), modified_date=datetime.datetime.now(),
            status=False)
        
        rdb.session.add(p)
        rdb.session.commit()
        return {"status_code" : 200, "message":"Post product completely!"}

@bp.route('/modify/<int:product_id>',methods=["POST"])
def modify_product(product_id):
    # TODO User check
    
    # Modify the data
    body = request.get_json() 
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
    p = ProductModel.query.get(product_id)
    rdb.session.delete(p)
    rdb.session.commit()

    return {"status_code" : 200, "message":"Delete product completely!"}



        
        
        
        
