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
#   OUTPUT: Shell scripts for spooling to each AWS environment, and copies of the input files ready to email
#   in the export folder.

#   SET THESE VALUES before running - must point to existing folders
#   filesystem root (needed because Ubuntu on Windows...)
fs_root = "/c/"

#   folder where TSV input files are stored
input_folder = "ndla-spoling/"

#   folder where TSV files are copied to and renamed with pretty names for sending out mail
export_folder = "ndla-export/"


#   TSV files are downloaded from these URLs:

#    1  https://docs.google.com/spreadsheets/d/1q-A35Q_PZ6xsFx0EkeLHlDTuEQi2evNNB-pGfThiSPo/  medieuttrykk
#    2  https://docs.google.com/spreadsheets/d/16SW5rFxpOXBW7tJV-OuhpRBto1vcqZDQ6MMQSYWNvUI/  kinesisk
#    3  https://docs.google.com/spreadsheets/d/1srIDLu9B7pM4Yjvsu-7S2LBZBWK665rD1Qph5IevCPU/  samfunnsfag
#    4  https://docs.google.com/spreadsheets/d/16ZomBB7X56q2dhqxYurBB7ysU-a7Xja94K775i-pYzs/  helsearbeiderfag-vg2
#    5  https://docs.google.com/spreadsheets/d/1abr7bK8nbbiDai364QKx8-BYN5xgZC9yt0Vf-ElUQ8Y/  test

#    6  https://docs.google.com/spreadsheets/d/1XAwgVE4mTg-kGuB0jbEg05iJuHLBapPgZhd0pzrbjqc/  brønnteknikk
#    7  https://docs.google.com/spreadsheets/d/1LMIR3tVwR41ppkEjG-cVjf-yzelIOJtED_kaypvhblw/  markedsføring-og-ledelse
#    8  https://docs.google.com/spreadsheets/d/1D4YngL5Jb5so3Jdm4RrIRy43CVccRsu-NUw1FwH4jyY/  tysk
#    9  https://docs.google.com/spreadsheets/d/1YOxZ78mBBxZAl1I834oHhj1kqbF5n2OekDZDJT-HtQU/  historie
#   10  https://docs.google.com/spreadsheets/d/1KZFqT4KlFbDPVge0evUP5pR754tTpTE7lfRJpQNzDvw/  matematikk-fellesfag

#   11  https://docs.google.com/spreadsheets/d/1310W8EjeegGxKWuuF00lz44_XsTJNLTYxQzKJWyV_74/  bygg-og-anleggsteknikk
#   12  https://docs.google.com/spreadsheets/d/1_Gha5AVP5wUAfhF9BeQIWVyPElAsIReOYmVfZhedcyo/  service-og-samferdsel
#   13  https://docs.google.com/spreadsheets/d/1g_dV6vesx61XOQGBPL0xW4lDGmFxGWJD2ZsrDy0nP-8/  naturbruk
#   14  https://docs.google.com/spreadsheets/d/1I7r0SRG0NXr2ML6wYffEr-bkoURj-ERdA88h1Vu9oq0/  medie-og-informasjonskunnskap
#   15  https://docs.google.com/spreadsheets/d/19IClAPKQloxKn-iTs1Bnnaden34-lNgoVEAmVtKxMh0/  sørsamisk

#   16  https://docs.google.com/spreadsheets/d/1CX6oEo8YPCY3j_Dv8spnxs4NsI59LFfwfDF6enAcMac/  elektrofag
#   17  https://docs.google.com/spreadsheets/d/1JR4zsKyh7LC-vMG-EC-X_ovpg14IbUH3Qa3Z-flF5m0/  engelskspråklig-litteratur-og-kultur
#   18  https://docs.google.com/spreadsheets/d/1FnptdafPRBH5cqzjAf_JOZD2zz25W8kUMFIpUJ1v910/  kommunikasjon-og-kultur
#   19  https://docs.google.com/spreadsheets/d/1V8PNUocFJ09s63uWjL88UACFcIYh0xWlyhInlp9cG_Q/  norsk
#   20  https://docs.google.com/spreadsheets/d/121vfBZR9RvwFfYD8rqZfQEMSxGSvKj1zstNa2Jiu3ZI/  NDLA film
#
#   21  https://docs.google.com/spreadsheets/d/1SXV7O7xwKbyi7tUc86mYwYk3c9gLnSUVwOYNH8quBEw/  naturfag
#   22  https://docs.google.com/spreadsheets/d/1cU1B8QbFAl3d-9uD2E2kK4HZoKWxpCxgkkiQJSgnKQ4/  salg-service-sikkerhet
#   23  https://docs.google.com/spreadsheets/d/1pJH4KE1FA1W9nnQ0vkgIpZGCrksg0aEjtrNUGHCxtTg/  samfunnsfaglig-engelsk
#   24  https://docs.google.com/spreadsheets/d/1TldbFJRmIIcVCBFUO5F1Ae8NJHyrUy0pN6n-D8wZr50/  helse-og-oppvekst
#   25  https://docs.google.com/spreadsheets/d/1dHnhasaBYDoF9768FLlHajt0hnp0KoEpxrCrYiNzZ1A/  ikt-servicefag
#
#   26  https://docs.google.com/spreadsheets/d/1XGiUf6GdYbIgOPYZ4iLDyIttOPKD43BPKpy526ELq8A/  kroppsøving
#   27  https://docs.google.com/spreadsheets/d/17YfYge4G8KFXeLDC0BJCfdjBlWeYDYIlIsSM1GlxYDY/  internasjonal engelsk
#   28  https://docs.google.com/spreadsheets/d/1F5jj5287R57Z-709bfFYBKykr5W-JYoO3fxMctlvoGs/  teknikk og industriell produksjon
#   29  https://docs.google.com/spreadsheets/d/1imO2qaSVbQLpq24cnt8rzlZSMr7tigdDOHr--AMyt_Q/  praktisk matematikk
#   30  https://docs.google.com/spreadsheets/d/1EhmDHgeaS76dvSeFCV2dVLbMpps3FLN1gwaMsRMmtn0/  Matematikk for yrkesfaglige programmer
#   31  https://docs.google.com/spreadsheets/d/1p5LtP-nSpp4L81nRL6uThDOPftb41h9rUdVt5rb8VJ8/  Matematikk for samfunnsfag
#   32  https://docs.google.com/spreadsheets/d/1fs8zarUb28VnzRsdarO6D0u45eRaI-LAn82ez18ng1o/  Matematikk for realfag
#   33  https://docs.google.com/spreadsheets/d/1dtLKlULCspVKxXod3TEzjiJRtNPBFvPWAG2qq1HxKEw/  1T - Matematikk fellesfag
#   34  https://docs.google.com/spreadsheets/d/1t49dUUQFnrAKnmq8xftIzHkMbY08fQ9i7b-1lhi6CVc/  1P - Matematikk fellesfag
#   35  https://docs.google.com/spreadsheets/d/1UtiPGBPS-h3VUdUejPRJjk0Mi1yad4SOHXjwUYxC024/  Reiseliv
#   36  https://docs.google.com/spreadsheets/d/1ny0gSl57scuev-MCs7kXBmdKj-eEJqqYP4NuP0CI7LA/  Transport og logistikk
#   37  https://docs.google.com/spreadsheets/d/1H4EBhvVpaK9tnHdagm7X54YR_orYgg2A4Ij70SvSiF8/  Restaurant- og matfag Vg1
#   38  https://docs.google.com/spreadsheets/d/1BfP-RZ3NfhWjO87nFe6JqcdfBG2wK6zvbCTt_5MExu0/  Design og håndverk Vg1
#   39  https://docs.google.com/spreadsheets/d/1vk9-mASASr-UpPKy-N_08EcDRd1EpD1_2nVaq-fe_bM/  Engelsk Vg1
#   40  https://docs.google.com/spreadsheets/d/15oLpaisgmSKHjjK_6wKHuccVRMHpPGf_MwlykTeWjZE/  BUA-faget
#   41  https://docs.google.com/spreadsheets/d/17QSXFo0qguVNkykZRP5QsdStdWl2oY2TLlGLES9DPvk/  Kokk- og servitørfaget VG2
#   42  https://docs.google.com/spreadsheets/d/1pcWpSDSARt9M64Fo35S7uLm1cc2oabwWshi1miS3xBA/  Biologi
#   43  https://docs.google.com/spreadsheets/d/1gYPHFkA0Ji4qYhbW41hUTlOcvOatFcoYDJKXr3C7Kng/  Sosiologi og sosialantropologi
#   44  https://docs.google.com/spreadsheets/d/1iO2wjB_AeFep0lmhSfWrEK3rGWRGqP_D46COV06o9ek/  Religion og etikk

jar_location = "target/taxonomy-import.jar"

now = datetime.date.today()
year = str(now.year)
month = str(now.month).zfill(2)
day = str(now.day).zfill(2)
date = year + month + day

servers = [
    ("test", "http://ndla-taxonomy-test.uarauzeick.eu-central-1.elasticbeanstalk.com/"),
    ("brukertest", "http://ndla-taxonomy-brukertest.uarauzeick.eu-central-1.elasticbeanstalk.com/"),
    ("spoletest", "http://ndla-taxonomy-spoletest.eu-central-1.elasticbeanstalk.com/"),
    ("staging", "http://ndla-taxonomy-staging.uarauzeick.eu-central-1.elasticbeanstalk.com/"),
    ("prod", "http://ndla-taxonomy-prod.uarauzeick.eu-central-1.elasticbeanstalk.com/"),
    ("dev", "http://ndla-taxonomy-dev.eu-central-1.elasticbeanstalk.com/"),
    ("localhost", "http://localhost:5000/")
]

# Maps short names to "pretty names" for subjects, ordered by ID


subjects = [
    ("medieuttrykk", "Medieuttrykk og mediesamfunnet"),
    ("kinesisk", "Kinesisk"),
    ("samfunnsfag", "Samfunnsfag"),
    ("helsearbeiderfag-vg2", "Helsearbeiderfag Vg2"),
    ("test", "Testfag"),
    ("brønnteknikk", "Brønnteknikk"),
    ("markedsføring-og-ledelse", "Markedsføring og ledelse 1"),
    ("tysk", "Tysk"),
    ("historie", "Historie Vg2 og Vg3"),
    ("matematikk-fellesfag", "Matematikk fellesfag"),
    ("bygg-og-anleggsteknikk", "Bygg- og anleggsteknikk"),
    ("service-og-samferdsel", "Service og samferdsel Vg1"),
    ("naturbruk", "Naturbruk Vg1"),
    ("medie-og-informasjonskunnskap", "Medie- og informasjonskunnskap"),
    ("sørsamisk", "Sørsamisk"),
    ("elektrofag", "Elektrofag"),
    ("engelskspråklig-litteratur-og-kultur", "Engelskspråklig litteratur og kultur"),
    ("kommunikasjon-og-kultur", "Kommunikasjon og kultur"),
    ("norsk", "Norsk"),
    ("ndla-film", "NDLA film"),
    ("naturfag", "Naturfag"),
    ("salg-service-sikkerhet", "Salg, service og sikkerhet Vg2"),
    ("samfunnsfaglig-engelsk", "Samfunnsfaglig engelsk"),
    ("helse-og-oppvekst", "Helse- og oppvekstfag Vg1"),
    ("ikt-servicefag", "IKT-servicefag Vg2"),
    ("kroppsøving", "Kroppsøving"),
    ("internasjonal-engelsk", "Internasjonal engelsk"),
    ("teknikk-og-industriell-produksjon", "Teknikk og industriell produksjon"),
    ("praktisk-matematikk", "Praktisk matematikk"),
    ("matematikk-for-yrkesfag", "Matematikk for yrkesfaglige programmer"),
    ("matematikk-for-samfunnsfag", "Matematikk for samfunnsfag"),
    ("matematikk-for-realfag", "Matematikk for realfag"),
    ("matematikk-1T-fellesfag", "1T - Matematikk fellesfag"),
    ("matematikk-1P-fellesfag", "1P - Matematikk fellesfag"),
    ("reiseliv", "Reiseliv"),
    ("transport-og-logistikk", "Transport og logistikk"),
    ("restaurant-og-matfag", "Restaurant- og matfag Vg1"),
    ("design-og-håndverk", "Design og håndverk Vg1"),
    ("engelsk", "Engelsk Vg1"),
    ("bua-faget","BUA-faget"),
    ("kokk-og-servitør-faget", "Kokk- og servitørfag Vg2"),
    ("biologi", "Biologi"),
    ("sosiologi-og-sosialantropologi", "Sosiologi og sosialantropologi"),
    ("religion-og-etikk", "Religion og etikk")
]


def make_filename(id):
    subject_short_name = subjects[id][0]
    return "ndla-" + str(id + 1).zfill(2) + "-" + subject_short_name + "-" + date + ".tsv"


def copy_renamed_input_files():
    current_dir = os.getcwd()
    os.chdir(fs_root+input_folder)
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
                    "cat {} | {} -i {} -ci CLIENT_ID -cs CLIENT_SECRET -ts TOKEN_SERVER -n {} -e {} &> {}\n".format(input_file, jar_location, subject_id, subject_name,
                                                                  environment, logfile))
            script.close()


copy_renamed_input_files()
create_shell_scripts()
