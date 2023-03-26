
def model2json(model):
    q_dict = {}
    for col in model.__table__.columns:
        q_dict[col.name] = str(getattr(model, col.name))
    return q_dict