/** Daneel Yaitskov */
class Cmd {

    /**
     * Fork exec wait
     */
    static few(String command) {
        def proc = command.execute()
        def ecode = proc.waitFor()
        if (ecode) {
            throw new IOException("""command '$command' failed
with code $ecode:\n ${proc.err.text}\noutput:\n${proc.text}""")
        }
        proc.text
    }
}
