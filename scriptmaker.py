import codecs
import datetime
import glob
import os
import shutil

#
#   This script is intended to help spooling from TSV files in a local folder
#   to the various AWS environments for NDLA.
#
#   EXPECTED INPUT: A folder where the TSV files to import are saved as "1.tsv", "2.tsv" etc.
#   according to the following list

#    1  https://docs.google.com/spreadsheets/d/1q-A35Q_PZ6xsFx0EkeLHlDTuEQi2evNNB-pGfThiSPo/
#    2  https://docs.google.com/spreadsheets/d/16SW5rFxpOXBW7tJV-OuhpRBto1vcqZDQ6MMQSYWNvUI/
#    3  https://docs.google.com/spreadsheets/d/1srIDLu9B7pM4Yjvsu-7S2LBZBWK665rD1Qph5IevCPU/
#    4  https://docs.google.com/spreadsheets/d/16ZomBB7X56q2dhqxYurBB7ysU-a7Xja94K775i-pYzs/
#    5  https://docs.google.com/spreadsheets/d/1abr7bK8nbbiDai364QKx8-BYN5xgZC9yt0Vf-ElUQ8Y/

#    6  https://docs.google.com/spreadsheets/d/1XAwgVE4mTg-kGuB0jbEg05iJuHLBapPgZhd0pzrbjqc/
#    7  https://docs.google.com/spreadsheets/d/1LMIR3tVwR41ppkEjG-cVjf-yzelIOJtED_kaypvhblw/
#    8  https://docs.google.com/spreadsheets/d/1D4YngL5Jb5so3Jdm4RrIRy43CVccRsu-NUw1FwH4jyY/
#    9  https://docs.google.com/spreadsheets/d/1YOxZ78mBBxZAl1I834oHhj1kqbF5n2OekDZDJT-HtQU/
#   10  https://docs.google.com/spreadsheets/d/1KZFqT4KlFbDPVge0evUP5pR754tTpTE7lfRJpQNzDvw/

#   11  https://docs.google.com/spreadsheets/d/1310W8EjeegGxKWuuF00lz44_XsTJNLTYxQzKJWyV_74/
#   12  https://docs.google.com/spreadsheets/d/1_Gha5AVP5wUAfhF9BeQIWVyPElAsIReOYmVfZhedcyo/
#   13  https://docs.google.com/spreadsheets/d/1g_dV6vesx61XOQGBPL0xW4lDGmFxGWJD2ZsrDy0nP-8/
#   14  https://docs.google.com/spreadsheets/d/1I7r0SRG0NXr2ML6wYffEr-bkoURj-ERdA88h1Vu9oq0/
#   15  https://docs.google.com/spreadsheets/d/19IClAPKQloxKn-iTs1Bnnaden34-lNgoVEAmVtKxMh0/

#   16  https://docs.google.com/spreadsheets/d/1CX6oEo8YPCY3j_Dv8spnxs4NsI59LFfwfDF6enAcMac/
#   17  https://docs.google.com/spreadsheets/d/1JR4zsKyh7LC-vMG-EC-X_ovpg14IbUH3Qa3Z-flF5m0/
#   18  https://docs.google.com/spreadsheets/d/1FnptdafPRBH5cqzjAf_JOZD2zz25W8kUMFIpUJ1v910/
#
#   OUTPUT: Shell scripts for spooling to each AWS environment, and copies of the input files ready to email
#   in the export folder.
#
#   Author: oystein.ovrebo@bouvet.no

#   SET THESE VALUES before running - must point to existing folders
input_folder = "ndla-spoling-input/"
export_folder = "ndla-spoling-export/"
fs_root = "/mnt/c/"

jar_location = "target/taxonomy-import.jar"

now = datetime.date.today()
year = str(now.year)
month = str(now.month).zfill(2)
day = str(now.day).zfill(2)
date = year + month + day

servers = [
    ("test", "http://ndla-taxonomy-test.uarauzeick.eu-central-1.elasticbeanstalk.com/"),
    ("brukertest", "http://ndla-taxonomy-brukertest.uarauzeick.eu-central-1.elasticbeanstalk.com/"),
    ("staging", "http://ndla-taxonomy-staging.uarauzeick.eu-central-1.elasticbeanstalk.com/"),
    ("prod", "http://ndla-taxonomy-prod.uarauzeick.eu-central-1.elasticbeanstalk.com/")
]

# Maps short names to "pretty names" for subjects, ordered by ID
subjects = [
    ("medieuttrykk", "Medieuttrykk og mediesamfunnet"),
    ("kinesisk", "Kinesisk"),
    ("samfunnsfag", "Samfunnsfag"),
    ("helsearbeiderfag-vg2", "Vg 2 Helsearbeiderfag"),
    ("test", "Testfag"),
    ("brønnteknikk", "Brønnteknikk"),
    ("markedsføring-og-ledelse", "Markedsføring og ledelse 1"),
    ("tysk", "Tysk"),
    ("historie", "Historie vg2 og vg3"),
    ("matematikk-fellesfag", "Matematikk fellesfag"),
    ("bygg-og-anleggsteknikk", "Bygg- og anleggsteknikk"),
    ("service-og-samferdsel", "Service og samferdsel vg1"),
    ("naturbruk", "Naturbruk vg 1"),
    ("medie-og-informasjonskunnskap", "Medie- og informasjonskunnskap"),
    ("sør-samisk", "Sørsamisk vg1 og vg2"),
    ("elektrofag", "Elektrofag vg1"),
    ("engelskspråklig-litteratur-og-kultur", "Engelskspråklig litteratur og kultur"),
    ("kommunikasjon-og-kultur", "Kommunikasjon og kultur 1, 2, 3")
]


def make_filename(id):
    subject_short_name = subjects[id][0]
    return "ndla-" + str(id + 1).zfill(2) + "-" + subject_short_name + "-" + date + ".tsv"


def copy_renamed_input_files():
    current_dir = os.getcwd()
    os.chdir(input_folder)
    for file in glob.glob("*.tsv"):
        end_index = file.find(".tsv")
        name = file[:end_index]
        index_from_name = int(name) - 1
        new_name = make_filename(index_from_name)
        if not os.path.exists("../" + export_folder):
            os.mkdir("../" + export_folder)
        shutil.copy2(file, "../" + export_folder + "/" + new_name)
    os.chdir(current_dir)


def create_shell_scripts():
    for server in servers:
        with codecs.open(server[0] + ".sh", "w", "utf-8-sig") as script:
            script.write("#!/bin/sh\n\n")
            for idx, subject in enumerate(subjects):
                id = str(idx + 1)
                path = fs_root + input_folder
                input_file = path + id + ".tsv"
                subject_id = 'urn:subject:' + id
                subject_name = "\"" + subject[1] + "\""
                logfile = '{}{}-{}-{}-{}.log'.format(path, id, subject[0], date, server[0])
                environment = server[1]
                script.write(
                    "cat {} | {} -i {} -n {} -e {} > {}\n".format(input_file, jar_location, subject_id, subject_name,
                                                                  environment, logfile))
            script.close()


copy_renamed_input_files()
create_shell_scripts()
