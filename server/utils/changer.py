
def model2json(model):
    q_dict = {}
    for col in model.__table__.columns:
        q_dict[col.name] = str(getattr(model, col.name))
    return q_dict

def res_msg(status, message):
    return {"status_code":status, "message":message}