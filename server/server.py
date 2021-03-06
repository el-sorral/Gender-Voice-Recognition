import os
import socket

from flask import Flask, request, redirect, send_from_directory, flash
from werkzeug.utils import secure_filename

RSERVER_HOST = os.getenv('RSERVER_HOST', 'localhost')
print "hola"
print RSERVER_HOST
RSERVER_PORT = 5001

UPLOAD_FOLDER = './uploads'
ALLOWED_EXTENSIONS = set(['wav'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


@app.route("/")
def hello():
    return "Hello World!"


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/verify', methods=['POST'])
def verify_gender():
    filename = request.form.get('filename')
    gender = request.form.get('gender')
    return generate_file_csv(filename, gender)


@app.route('/upload', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        if 'file' not in request.files:
            flash('No file part')
            return redirect(request.url)
        file = request.files['file']
        if file.filename == '':
            flash('No selected file')
            return redirect(request.url)
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            gender = predict(filename)
            return gender[:-1]
    return '''
        <!doctype html>
        <title>Upload new File</title>
        <h1>Upload new File</h1>
        <form method=post enctype=multipart/form-data>
          <p><input type=file name=file>
             <input type=submit value=Upload>
        </form>
        '''


@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)


def predict(filename):
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((RSERVER_HOST, RSERVER_PORT))
    client_socket.send('predict ' + filename + '\n')
    data = client_socket.recv(2048)
    client_socket.close()
    return data


def generate_file_csv(filename, gender):
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((RSERVER_HOST, RSERVER_PORT))
    client_socket.send('store ' + filename + ' ' + gender + '\n')
    client_socket.close()
    return 'stored'


# def predict(filename):
# generate_csv(filename)
# bashCommand = "Rscript predict.R 2> /dev/null"
# process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
# output, error = process.communicate()
# return output


# def generate_csv(filename):
#     bashCommand = "Rscript extractor.R uploads/" + filename + " female 2> /dev/null > test.csv"
#     with open('test.csv', 'w') as outfile:
#         subprocess.call(bashCommand.split(), stdout=outfile)


if __name__ == "__main__":
    app.run(
        host=app.config.get("HOST", "0.0.0.0"),
        port=app.config.get("PORT", 5000)
    )
