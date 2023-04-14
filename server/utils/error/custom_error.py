from flask import Response,jsonify
import json

class CustomException(Exception):
    def __init__(self, status_code, message):
        self.status_code = status_code
        self.message = message

# DB Exception 
class DBConnectionError(CustomException):
    def __init__(self):
        super().__init__(status_code=503, message="DB Connection Error")

class DBNotFound(CustomException):
    def __init__(self, target):
        super().__init__(status_code=404, message=f"{target} Not Found in DB")

# Parameters 
class MissingParams(CustomException):
    def __init__(self, param):
        super().__init__(status_code=400, message=f"Missing Parameter : {param}")

class EmptyError(CustomException):
    def __init__(self, target):
        super().__init__(status_code=400, message=f"Empty {target}")

class OutOfBound(CustomException):
    def __init__(self):
        super().__init__(status_code=404, message="Out of Bund")

# payment 
class InvalidPaymentAuthorization(CustomException):
    def __init__(self):
        super().__init__(status_code=404, message="Invalid Payment Authorization.")

def error_handler(error):
    resp = {
        'status': error.status_code,
        'message': error.message
    }
    return jsonify(resp), error.status_code


def init_custom_error_handler(app):
    app.register_error_handler(CustomException, error_handler)
    app.register_error_handler(DBConnectionError, error_handler)
    app.register_error_handler(DBNotFound, error_handler)
    app.register_error_handler(MissingParams, error_handler)
    app.register_error_handler(EmptyError, error_handler)
    app.register_error_handler(OutOfBound,error_handler)
    app.register_error_handler(InvalidPaymentAuthorization, error_handler)

    

