from flask import Flask, render_template, jsonify, request

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/add_personnel')
def addPersonnel():
    return render_template('add_personnel.html')

@app.route('/assign_room')
def assignRoom():
    return render_template('assign_room.html')

@app.route('/running')
def running():
    return render_template('running.html')

# @app.route('/<name>')
# def hello(name):
#     return render_template('name.html', name = name)

# @app.route('/_add_numbers')
# def add_numbers():
#     a = request.args.get('a', 0, type=int)
#     b = request.args.get('b', 0, type=int)
#     return jsonify(result=a + b)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')