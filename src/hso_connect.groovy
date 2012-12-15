#!/usr/bin/groovy
/**
 *
 * This program is port hso_connect.sh for my Beeline USB model.
 * I want to get experience with groovy as replacement for bash.
 *
 * Let's describe core functionality.
 * It's a daemon. There is only 1 goal keep internet connection live.
 * 3G ISPs are not reliable enough.
 *
 *
 * Base entities in program: modem, connection, test message.
 *
 * Daneel Yaitskov
 * */

 println "Starting"

con = new Modem()
tester = new Tester()
// number fails in line
fails = 0
while (1) {
    if (tester.isConnected()) {
        fails = 0
        sleep(20000) // 20 seconds
        continue
    }
    fails ++
    con.up()
    println "Awaiting"
    sleep(fails * 1000 + 2000)
}