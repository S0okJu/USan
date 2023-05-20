import os, sys
import json 
import datetime

# * lib
from flask import request, jsonify, Blueprint, redirect
from flask_jwt_extended import jwt_required,get_jwt_identity
import requests
import sqlalchemy.exc

# * User defined
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from init.init_db import rdb
import utils.error.custom_error as error
from models import PaymentRefreshToken

# * Test이기 때문에 별도로 Client_id와 password는 고정. 
CLIENT_ID = '1d1099e6-8c17-4ac3-956c-2e9bd6039c19'
CLIENT_PASSWORD = 'e51b7de9-9647-4760-8707-e77e7a53bce6'
STATE_CODE = '12345678901234567890124456729112'
REDIRECT_URI = 'http://127.0.0.1:6000/payment/callback'
URI_BASE = 'https://testapi.openbanking.or.kr/oauth/2.0'

# Sample로 남겨둠
CODE='sN2jZw9KyttfKTgBAdAPU2lJORJhgv'
ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAxMDI3NjYxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2OTIzNDYyMzIsImp0aSI6ImUxOGMzY2E5LTA4MmMtNDE4YS05NmEzLTU1YjA5MWQzOTYxYSJ9.ZgKJ4duMYQdI19arcc4jYRthaYG4eB5Ai4b9QQuTlrE"
REFRESH_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAxMDI3NjYxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2OTMyMTAyMzIsImp0aSI6IjAwNDNjOTQwLThiZGItNDQ2YS04MTBlLTZhZjIxNzAxNGFhNSJ9.f9yfcH5o8V15HQbpwQRKXIYxLDGbb8uYZxTx5QfzENo'
SEQ_NO = 1101027661
bp = Blueprint('payment', __name__, url_prefix='/payment')

@bp.route('/auth',methods=["POST"])
# @jwt_required()
def authorization():
    # https://testapi.openbanking.or.kr/oauth/2.0/authorize?response_type=code&client_id=1d1099e6-8c17-4ac3-956c-2e9bd6039c19&redirect_uri=http://127.0.0.1:6000/payment/callback&scope=login+inquiry+transfer&state=12345678901234567890124456729112&auth_type=0&cellphone_cert_yn=Y&authorized_cert_yn=N
    
    auth_url = f'https://testapi.openbanking.or.kr/oauth/2.0/authorize?response_type=code&client_id={CLIENT_ID}&redirect_uri={REDIRECT_URI}&scope=login+inquiry+transfer&state={STATE_CODE}&auth_type=0&cellphone_cert_yn=Y&authorized_cert_yn=N'
    print(auth_url)
    try:
        authorization_redirect = requests.Request('GET', auth_url).prepare()
        return redirect(authorization_redirect.url)

    except requests.exceptions.Timeout as e:
        print("Timeout Error : ", e)
    except requests.exceptions.ConnectionError as e:
        print("Error Connecting : ", e)
    except requests.exceptions.HTTPError as e:
        print("Http Error : ", e)
    except requests.exceptions.RequestException as e:
        print("AnyException : ", e)


@bp.route("/callback",methods=["GET"])
def callback():
    # Authorization Code 추출
    authorization_code = request.args.get('code')

    # Access Token 발급 요청
    token_url = f"{URI_BASE}/token"
    data = {
        'grant_type': 'authorization_code',
        'code': CODE,
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_PASSWORD,
        'redirect_uri': REDIRECT_URI
    }
    token_response = requests.post(token_url, data=data)
    print(token_response)
    return jsonify(token_response.json()),200

@bp.route("/refresh", methods=["GET"])
def refresh():
    data = {
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_PASSWORD,
        "refresh_token" : REFRESH_TOKEN,
        "scope" : "login inquiry transfer",
        "grant_type":"refresh_token",
    }
    res = requests.post("https://testapi.openbanking.or.kr/oauth/2.0/token",data=data)
    print(res.json())
    return jsonify(res.json()),200 


@bp.route("/transfer",methods=["POST"])
def transfer():
    
    res = request.get_json()
    headers = {"Authorization":ACCESS_TOKEN}
    # body = {
    #             'cntr_account_type' : "N",
    #             'cntr_account_num' : '995002320103',
    #             'dps_print_content' : "이용료",
    #             "fintech_use_num": "120230064688951050013697",
    #             'tran_amt' : res['price'],
    #             'tran_dtime' : datetime.datetime.now().strftime('%Y%m%d'),
    #             'req_client_num' : 'kimkim',
    #             'req_client_bank_code' : "097",
    #             'req_client_name' : res['name'],
    #             'transfer_purpose' : "TR",
    #             'recv_client_bank_code' : "097",
    #         }

    body2 = {
        "bank_tran_id":"M202300646U000000000",         
        "cntr_account_type":"N",
        "cntr_account_num":"995002320103",
        "dps_print_content":"쇼핑몰환불",
        "fintech_use_num":"123456789012345678901234",
        "wd_print_content":"오픈뱅킹출금",
        "tran_amt":"10000",
        "tran_dtime":datetime.datetime.now().strftime('%Y%m%d'),
        "req_client_name": "홍길동",
        "req_client_bank_code":"097",
        "req_client_account_num": "264001920103",
        "req_client_num": "hong",
        "transfer_purpose": "TR",
  
        "recv_client_name": "김김김",
        "recv_client_bank_code": "097",
        "recv_client_account_num": "264001920103"
    }
    res = requests.post('https://testapi.openbanking.or.kr/v2.0/transfer/withdraw/fin_num',data=body2,headers=headers)
    return jsonify(res.json()), 200


