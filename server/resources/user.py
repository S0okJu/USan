import os, sys
import datetime

from flask_restx import Resource

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import UserModel
from db.init_db import rdb

class RegisterUser(Resource):
    def post(self):
        # body = request.get_json() 
        # obj = json.loads(json.dumps(body))
        user = UserModel(email="sample@chosun.kr",password="12345",address="임시 주소",
            created_date = datetime.datetime.now(), modified_date=datetime.datetime.now())
        rdb.session.add(user)
        rdb.session.commit()
        return {"msg":"success"}