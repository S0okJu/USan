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
REDIRECT_URI = 'http://192.168.50.188:6000/payment/callback'
URI_BASE = 'https://testapi.openbanking.or.kr/oauth/2.0'

# Sample로 남겨둠
AUTH_CODE='CNKriKDdTrIM76lzuQqzAKT7qpxPru'
SAMPLE_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAxMDI3NjYxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2OTA1OTgyNzQsImp0aSI6ImYyMzYzMTA5LTQ4OGItNDFmYi1hYjM5LTY5ZDNiMTRjMmQxYyJ9.ldi1RWJaX9FEw8D2WcRBzqGnL0Iwy6-3sKXa2dX0_aQ"
SAMPLE_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAxMDI3NjYxIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2OTE0NjIyNzQsImp0aSI6IjQ3NGVkN2QyLTVjZWMtNGQ4NC04NTdjLTg2NWM4MGFkYWI2MSJ9.0-IR_LVpTKYC9lntxkEgUAmy3SPJWvxaL7v6yANYDUM"
SAMPLE_SEQ = "1101027661"
BANK_CODE = "M202300646"

bp = Blueprint('payment', __name__, url_prefix='/payment')

@bp.route('/auth',methods=["POST"])
# @jwt_required()
def authorization():
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
        'code': authorization_code,
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_PASSWORD,
        'redirect_uri': REDIRECT_URI
    }
    token_response = requests.post(token_url, data=data)
    print(token_response)



# 사용자 인증 정보 요청 
@bp.route("/userme", methods=["GET"])
# @jwt_required()
def get_userme():

    refresh_url = f"{URI_BASE}/token"
    data = {
        "client_id":CLIENT_ID,
        "client_secret":CLIENT_PASSWORD,
        "refresh_token":SAMPLE_REFRESH_TOKEN,
        "scope":"login inquiry transfer",
        "grant_type": "refresh_token"  
    }
    token_res = requests.post(refresh_url,data=data)
    access_token = token_res.json()
    print(access_token)
    # headers = {'Authorization': f'Bearer {access_token}'}
    # userme_url = f"{URI_BASE}/user/me?user_seq_no={SAMPLE_SEQ}"
    # res = requests.Request('GET', userme_url,headers=headers)
    return jsonify({"mes":"good"}),200


