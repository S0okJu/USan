import os, sys
import json 

from flask import request,Response, jsonify
from flask_restx import Resource

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from models import ProductModel, save

class Product(Resource):
    
    # 상품 정보 조회 
    def get(self, product_id):
        question = ProductModel.query.get(product_id)
        return question
        
    def patch(self,product_id):
        pass
    
    def delete(self, product_id):
        pass


# 사용자로부터 title, author, price, address, content을 얻는다.  
class MakeProduct(Resource):
    def post(self):
        result = {}
        body = request.get_json() 
        product_obj = json.loads(json.dumps(body))
        save(product_obj)
        


         
        
        
        
        
