import os
from dotenv import load_dotenv

from flask import Flask
from flask_restx import Api
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)

# init DB 
rdb = SQLAlchemy()
load_dotenv()
app.config['SECRET_KEY'] =  os.environ.get('SECRET_KEY')
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('SQLALCHEMY_DATABASE_URI')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = os.environ.get('SQLALCHEMY_TRACK_MODIFICATIONS')
rdb.init_app(app)

# Add resource 
api = Api(app)

if __name__ =="__main__":
    app.run(debug=True, host='0.0.0.0')