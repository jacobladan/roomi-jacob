from flask import Flask, render_template, jsonify, request, url_for, redirect
import time

app = Flask(__name__)

@app.route('/')
def index():
    #Put stuff here to stop card polling
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

@app.route('/poll_for_card')
def pollForCard():
    ## Needs to poll for +1 seconds for JS to catch up
    time.sleep(6)
    # if (successful) {
    #     return jsonify(gotCard='true')
    # } else {
    #     return jsonify(gotCard='false')
    # }
    return jsonify(gotCard='true')

@app.route('/add_personnel_to_db')
def addPersonnelToDB():
    name = request.args.get('name', 'name')
    accessLevel = request.args.get('accessLevel', '0')
    print(name)
    print(accessLevel)
    return render_template('/index.html')

@app.route('/add_room_to_db')
def addRoomToDB():
    name = request.args.get('name', 'name')
    accessLevel = request.args.get('accessLevel', '0')
    print(name)
    print(accessLevel)
    return render_template('/index.html')

@app.route('/stop_security')
def stopSecurity():
    return render_template('/index.html')

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