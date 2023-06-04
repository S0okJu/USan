import os, sys
import json 
import uuid

# * lib
from flask import request,make_response, jsonify, Blueprint, send_from_directory,send_file
import sqlalchemy.exc
import firebase_admin
from firebase_admin import credentials, auth, db 
from flask_jwt_extended import jwt_required, get_jwt_identity
import re

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, UserModel, AccountModel
from init.init_db import rdb
import utils.error.custom_error as error 

FIREBASE_URL = "https://usan-team-default-rtdb.firebaseio.com"
# Connection to Firebase 
cred = credentials.Certificate("./firebase/fbAdminConfig.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': FIREBASE_URL
})

bp = Blueprint('payments', __name__, url_prefix='/payments')

# chatId에서 숫자만 뽑는 정규식  
def extract_numbers(s):
    return "".join(re.findall(r'\d+', s))

@bp.route("/withdraw/<string:chat_id>", methods=['POST'])
# @jwt_required()
def withdraw(chat_id):
    try:
        # chat_id를 이용하여 Firebase로부터 거래 정보를 가져옵니다
        ref = db.reference(f'transaction/{chat_id}')
        data = ref.get()

        # 상품 정보를 얻습니다. 
        product_id = extract_numbers(chat_id)
        product = ProductModel.query.get(product_id)
        amount = product.price

        # Get user info                 
        seller_name = data['sellerName']
        buyer_name = data['buyerName']

        seller = UserModel.query.filter_by(username=seller_name).first().account
        buyer = UserModel.query.filter_by(username=buyer_name).first().account
        
        if seller.balance < amount:
            return jsonify({'error': 'Insufficient balance'}), 400

        seller.balance -= amount
        buyer.balance += amount

        rdb.session.commit()
        return jsonify({'success': True}), 200
    except Exception as e:

        return jsonify({'error': 'An error occurred'}), 500
    



