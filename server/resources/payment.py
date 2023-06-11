import os, sys
import json 
import uuid
import datetime
import random

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

bp = Blueprint('payment', __name__, url_prefix='/payment')

# chatId에서 숫자만 뽑는 정규식  
def extract_numbers(s):
    return "".join(re.findall(r'\d+', s))

@bp.route("/withdraw/<string:chat_id>", methods=['POST'])
@jwt_required()
def withdraw(chat_id):
    try:
        # chat_id를 이용하여 Firebase로부터 거래 정보를 가져옵니다
        ref = db.reference(f'transaction/{chat_id}')
        data = ref.get()

        # 상품 정보를 얻습니다. 
        product_id = extract_numbers(chat_id)
        print(product_id)
        product = ProductModel.query.get(product_id)
        amount = product.price

        # Get user info                 
        seller_name = data['sellerName']
        buyer_name = data['buyerName']

        seller = UserModel.query.filter_by(username=seller_name).first()
        buyer = UserModel.query.filter_by(username=buyer_name).first()
        seller_acc = AccountModel.query.filter_by(user_id=seller.user_id).first()
        buyer_acc = AccountModel.query.filter_by(user_id=buyer.user_id).first()

        if seller_acc is None or buyer_acc is None:
            return jsonify({'error': 'Account not found'}), 400

        if seller_acc.balance < amount:
            return jsonify({'error': 'Insufficient balance'}), 400

        seller_acc.balance -= amount
        buyer_acc.balance += amount

        # 상품 판매 완료 
        product.status = True 

        # 지불 정보 firebase에 저장 
        payments_ref = db.reference('payments')
        payment = {
            'buyer': buyer_name,
            'seller': seller_name,
            'product_id': product_id,
            'amount': amount,
            'date': datetime.datetime.utcnow().isoformat(),
        }
        payments_ref.child(chat_id).set(json.loads(json.dumps(payment, default=str)))
        rdb.session.commit()
        
        return jsonify({'success': True}), 200
    except Exception as e:
        print(e)
        return jsonify({'error': 'An error occurred'}), 500
    
    

def generate_number():
    part1 = str(random.randint(100, 999))  # Generate a random three-digit number
    part2 = str(random.randint(100, 999))  # Generate another random three-digit number
    part3 = str(random.randint(100000, 999999))  # Generate a random six-digit number
    return part1 + '-' + part2 + '-' + part3

# 임시 계ㅏ 
@bp.route("/create/<int:user_id>",methods=["GET"])
# @jwt_required()
def create_account(user_id):
    # user_id = get_jwt_identity()
    new_account = generate_number()
    print(new_account)
    account = AccountModel(user_id=user_id, account_number= new_account, balance=5000000)
    rdb.session.add(account)
    rdb.session.commit()
    return jsonify({'msg':'success'}),200


