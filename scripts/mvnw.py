"""Maven Wrapper in Python - downloads Maven then runs it"""
import os, sys, subprocess, urllib.request, zipfile, shutil

MVN_DIR = os.path.join(os.path.dirname(__file__), "..", ".mvn", "wrapper", "maven")
MVN_BIN = os.path.join(MVN_DIR, "apache-maven-3.9.6", "bin", "mvn.cmd")

if not os.path.exists(MVN_BIN):
    url = "https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    zip_path = MVN_DIR + ".zip"
    os.makedirs(MVN_DIR, exist_ok=True)
    print("Downloading Maven 3.9.6 ...")
    urllib.request.urlretrieve(url, zip_path)
    print("Extracting ...")
    with zipfile.ZipFile(zip_path, 'r') as z:
        z.extractall(MVN_DIR)
    os.remove(zip_path)
    print("Maven ready.")

# This project requires Java 17, override global JAVA_HOME
JAVA17_HOME = r"C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
if not os.path.isdir(JAVA17_HOME):
    # Fallback: find any jdk-17 in Program Files
    for d in os.listdir(r"C:\Program Files\Microsoft"):
        if d.startswith("jdk-17"):
            JAVA17_HOME = os.path.join(r"C:\Program Files\Microsoft", d)
            break
os.environ["JAVA_HOME"] = JAVA17_HOME
args = [MVN_BIN, "-f", os.path.join(os.path.dirname(__file__), "..", "pom.xml")] + sys.argv[1:]
subprocess.run(args, shell=True)
