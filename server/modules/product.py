import os
import json 
import datetime 
from flask_restx import Resource

from app import api

@api.route('/product/<string:product_id>')
class Product(Resource):
    def get(self, product_id):
        return {"test":"Hello WOrld"}
    
    def patch(self,product_id):
        pass
    
    def delete(self, product_id):
        pass
