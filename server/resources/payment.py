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

CLIENT_ID = '1d1099e6-8c17-4ac3-956c-2e9bd6039c19'
CLIENT_PASSWORD = 'e51b7de9-9647-4760-8707-e77e7a53bce6'
REDIRECT_URI = 'http://localhost:6000/payment/callback'
URI_BASE = 'https://testapi.openbanking.or.kr/oauth/2.0'


bp = Blueprint('payment', __name__, url_prefix='/payment')
@bp.route('/auth',methods=["POST"])
# @jwt_required()
def authorization():
    auth_url = f'https://testapi.openbanking.or.kr/oauth/2.0/authorize?response_type=code&client_id={CLIENT_ID}&redirect_uri={REDIRECT_URI}&scope=login+inquiry+transfer&state={STATE_CODE}&auth_type=0&cellphone_cert_yn=Y&authorized_cert_yn=N'
    
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


@bp.route("/callback")
def callback():
    # Authorization Code 추출
    authorization_code = request.args.get('code')

    # Access Token 발급 요청
    token_url = 'https://api.finerit.co.kr/v2.0/oauth2/token'
    data = {
        'grant_type': 'authorization_code',
        'code': 'client_credentials',
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_PASSWORD,
        'redirect_uri': REDIRECT_URI
    }
    token_response = requests.post(token_url, data=data)
    # TODO 
    
def get_token():
    pass