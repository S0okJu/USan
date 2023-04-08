import sys, os
from datetime import timedelta

from flask import Flask
from dotenv import load_dotenv

# custom 
from init.init_db import init_db
from resources import product, user,imgs, display
from init.init_jwt import init_jwt
from utils.error.custom_error import init_custom_error_handler

# Blueprint

# Flask 
app = Flask(__name__)
app.app_context().push()

# Error 
init_custom_error_handler(app=app)

# DB Setup 
load_dotenv()
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('SQLALCHEMY_DATABASE_URI')
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
init_db(app)

# File upload setup
app.config['ALLOWED_EXTENSIONS'] = set(['png', 'jpg', 'jpeg'])

# JWT Setup
# app.config['JWT_ACCESS_TOKEN_EXPIRES'] = timedelta(minutes=30)  
# app.config['JWT_REFRESH_TOKEN_EXPIRES'] = timedelta(days=30)  

app.config['JWT_BLACKLIST_TOKEN_CHECKS'] = ['access', 'refresh']
app.config['JWT_EXPIRATION_DELTA'] = timedelta(minutes=30)
app.config['JWT_NOT_BEFORE_DELTA'] = timedelta(minutes=5)
app.config['JWT_SECRET_KEY'] = 'usan#112'
init_jwt(app)

# Register the blueprint 
app.register_blueprint(product.bp)
app.register_blueprint(user.bp)
app.register_blueprint(imgs.bp)
app.register_blueprint(display.bp)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=6000, debug=True)
    