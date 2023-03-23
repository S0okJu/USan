from flask import Flask
from flask_restx import Api

from resources.product import * 

# Custom 
app = Flask(__name__)


api = Api(app)
api.add_resource(MakeProduct, '/product')
if __name__ =="__main__":
    app.run(debug=True, host='0.0.0.0')