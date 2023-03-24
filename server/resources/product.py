import os
import json 
import datetime 

from flask import request,Response, jsonify
from flask_restx import Resource

class Product(Resource):
    def get(self, product_id):
        pass
    
    def patch(self,product_id):
        pass
    
    def delete(self, product_id):
        pass

class MakeProduct(Resource):
    def post(self):
        result = {}
        body = request.get_json()
        product_obj = json.loads(json.dumps(body))
        product_obj['created_date'] = datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        product_obj['status'] = False
        pass 
        
        
        
        
