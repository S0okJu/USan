import os, sys
import json 
import datetime

# * lib
from flask import request,Response, jsonify, Blueprint, send_from_directory
from flask_jwt_extended import jwt_required
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, ProductImageModel
from init.init_db import rdb
import utils.color as msg
import utils.error.custom_error as error

bp = Blueprint('product', __name__, url_prefix='/product')

# 상품 정보 조회
# 특정 상품을 메인으로 볼때 사용된다.  
@bp.route('/<int:product_id>', methods=["GET"])
def get_product(product_id):
    try:
        question = ProductModel.query.get(product_id)
        if not question:
            raise error.DBNotFound('Product')
                
        q_dict = question.to_dict()
        author = UserModel.query.get(q_dict['author_id']) # get author name 
        del(q_dict['author_id'])
        q_dict['author'] = author 
        return Response(
            response = jsonify(q_dict),
            status=200,
            mimetype="application/json" 
        )
    
    except sqlalchemy.exc.SQLAlchemyError as e:
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
        author_data = UserModel.query.filter_by(username == obj['author']).first()
        if not author_data:
            raise error.DBNotFound('User')
                
        product_session = ProductModel(title=obj['title'], author=author_data,
            price=int(obj['price']),address=obj['address'], content=obj['content'],
            created_date= datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"), modified_date=datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            status=False,favorite=False)
        rdb.session.add(product_session)
        rdb.session.commit()
        return Response(
            response = jsonify({"message":"Success"}),
            status=200,
            mimetype="application/json"
        )
    
    except sqlalchemy.exc.SQLAlchemyError as e:
        print(e)
        raise error.DBConnectionError()


@bp.route('/modify',methods=["POST"])
def modify_product():
    # TODO User check using JWT Token 
    
    # Modify the data
    body = request.get_json() 
    if not body:
        raise error.Empty('JSON')

    obj = json.loads(json.dumps(body))
    p = ProductModel.query.get(obj['product_id'])
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
    return jsonify({"status_code" : 200, "message":"Modify product completely!"})

@bp.route('/delete/<int:product_id>',methods=["GET"])
def delete(product_id):
    # TODO User check using JWT Token 
    
    p = ProductModel.query.get(product_id)
    if not p:
        raise error.DBNotFound('Product')
    rdb.session.delete(p)
    rdb.session.commit()

    return jsonify({"status_code" : 200, "message":"Success"})

@bp.route("/favorite",methods=["GET"])
def check_favorite():
    product = request.args.get('product_id')
    
    if product:
        product.favorite = True
        rdb.session.commit()
    else:
        raise error.DBNotFound('Product')
    return jsonify({"status_code" : 200, "message":"Success"}), 200 

@bp.route('/status', methods=["GET"])
def check_status():
    product = request.args.get('product_id')
    
    if product:
        product.status = True
        rdb.session.commit()
    else:
        raise error.DBNotFound('Product')
    return jsonify({"status_code" : 200, "message":"Success"}), 200 