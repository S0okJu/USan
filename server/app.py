import os

from flask import Flask 
from flask_migrate import Migrate
from flask_restx import Api

from db.init_db import init_db
from resources import product, user
from dotenv import load_dotenv

app = Flask(__name__)


# DB Setup 
load_dotenv()
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('SQLALCHEMY_DATABASE_URI')
# app.config['SQLALCHEMY_BINDS'] ={
#     'RDB' : os.environ.get('SQLALCHEMY_DATABASE_URI')
# }
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

init_db(app)

# Add Resource 
api = Api(app)
api.add_resource(product.Product, '/product/<int:product_id>')
api.add_resource(product.MakeProduct, '/product')
api.add_resource(user.RegisterUser, '/user')

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
    