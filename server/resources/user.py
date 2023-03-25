import os, sys
import datetime
import json 

from flask import request,Blueprint

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import UserModel
from db.init_db import rdb

bp = Blueprint('user', __name__, url_prefix='/user')

@bp.route('/register', methods=["POST"])
def register():
    body = request.get_json() 
    obj = json.loads(json.dumps(body))
    
    user = UserModel(username=obj['username'],email=obj['email'],password=obj['password'],address=obj['address'],
        created_date = datetime.datetime.now(), modified_date=datetime.datetime.now())
    rdb.session.add(user)
    rdb.session.commit()
    return {"status_code" : 200, "message":"Post product completely!"}