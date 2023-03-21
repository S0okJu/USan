from flask import Flask
from flask_restx import Api
# Custom 

app = Flask(__name__)
api = Api(app)

if __name__ =="__main__":
    app.run(debug=True, host='0.0.0.0')