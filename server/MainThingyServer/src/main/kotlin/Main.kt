import app.NyanThingyCoapServer
import app.config.CoapDtlsConfig

/* Application entry point */
fun main(args: Array<String>) {

    //apply configs
    CoapDtlsConfig().apply()

    //starting the server
    NyanThingyCoapServer().start()
    return
}
