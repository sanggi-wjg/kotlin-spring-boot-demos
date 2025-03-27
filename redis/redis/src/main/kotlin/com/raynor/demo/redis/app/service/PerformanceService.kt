package com.raynor.demo.redis.app.service

import com.raynor.demo.redis.app.compress.Compressor
import com.raynor.demo.redis.app.compress.Lz4Compressor
import com.raynor.demo.redis.app.config.RedisTemplateConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class PerformanceService(
    private val valueOps: ValueOperations<String, String>,
    @Qualifier(RedisTemplateConfig.BEAN_NAME_VALUE_OPS_WITH_BYTES)
    private val valueOpsWithByteArray: ValueOperations<String, ByteArray>,
    @Qualifier(Lz4Compressor.BEAN_NAME) private val lz4Compressor: Compressor,
) {

    companion object {
        const val CYCLE_COUNT = 1

        const val TEST_ONE_KEY = "test-one"
        const val TEST_TWO_KEY = "test-two"
    }

    fun performTask1() {
        // Normal
        repeat(CYCLE_COUNT) {
            valueOps.set("$TEST_ONE_KEY-$it", getLoremIpsum(), 1, TimeUnit.HOURS)
        }
        repeat(CYCLE_COUNT) {
            valueOps.getAndDelete("$TEST_ONE_KEY-$it")
        }

        // Using LZ4
        repeat(CYCLE_COUNT) {
            val compressed = lz4Compressor.compress(getLoremIpsum())
            valueOpsWithByteArray.set("$TEST_TWO_KEY-$it", compressed, 1, TimeUnit.HOURS)
        }
        repeat(CYCLE_COUNT) {
            valueOpsWithByteArray.getAndDelete("$TEST_TWO_KEY-$it")
        }
    }

    fun performTask2() {
        // Normal
        repeat(CYCLE_COUNT) {
            valueOps.set("$TEST_ONE_KEY-$it", getXML(), 1, TimeUnit.HOURS)
        }
        repeat(CYCLE_COUNT) {
            valueOps.getAndDelete("$TEST_ONE_KEY-$it")
        }

        // Using LZ4
        repeat(CYCLE_COUNT) {
            val compressed = lz4Compressor.compress(getXML())
            valueOpsWithByteArray.set("$TEST_TWO_KEY-$it", compressed, 1, TimeUnit.HOURS)
        }
        repeat(CYCLE_COUNT) {
            val a = valueOpsWithByteArray.getAndDelete("$TEST_TWO_KEY-$it")
            println(a)
        }
    }

    private fun getLoremIpsum(): String {
        // https://www.lipsum.com/
        return """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis ac turpis egestas, ornare lacus ut, placerat leo. Curabitur libero neque, aliquet ut turpis nec, malesuada mollis ex. Pellentesque ut lacus in velit tincidunt lobortis sed interdum nulla. Quisque ultricies luctus odio ut mollis. Proin et libero volutpat, semper mauris a, viverra nunc. Nullam posuere rhoncus leo at malesuada. Vestibulum pulvinar porttitor felis, sit amet posuere risus hendrerit vel. Nullam nec volutpat ex, id ornare lectus. Suspendisse potenti. Quisque sed nunc quis urna ornare pharetra nec non nisl. Praesent consequat pulvinar erat quis hendrerit.

            Maecenas sollicitudin tincidunt molestie. Aliquam lacinia tortor elit, vel finibus tortor pellentesque vitae. Fusce varius tellus vel purus mattis, nec porttitor tortor viverra. Ut hendrerit diam diam, at aliquam risus iaculis non. Quisque vel fringilla mi. Vestibulum in urna mauris. Pellentesque vehicula maximus sem, sit amet malesuada libero suscipit eget. Integer sit amet ultrices dui, sed dictum erat. Quisque at fringilla est, sed aliquet ante. Maecenas ac mi vitae tellus scelerisque fringilla. Pellentesque mollis consequat tortor, sed vulputate sem ultricies id. Maecenas placerat purus at tempus cursus. In fermentum risus condimentum, sagittis leo sed, rhoncus ipsum. Nulla a magna tempor, tincidunt nisi in, convallis nisl. Morbi a risus id nisl scelerisque semper. Donec non libero in purus dapibus posuere eu vel metus.

            Aenean laoreet pulvinar pretium. Sed a tincidunt sem. Etiam sodales commodo nunc vitae luctus. Etiam ut tristique mi. Mauris laoreet orci ut augue semper congue. Donec ullamcorper enim nisl, bibendum scelerisque diam ultricies eget. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Praesent rhoncus, nisl eget maximus condimentum, risus risus faucibus diam, vitae hendrerit erat felis vitae tellus. Duis tincidunt, quam vitae elementum pulvinar, odio lacus hendrerit augue, eget posuere odio dolor a dui. Praesent suscipit elementum risus eu convallis.

            Vivamus sed ante et lacus vestibulum tempus nec id dolor. Etiam eleifend velit ex, sit amet auctor neque vehicula a. Nunc sollicitudin enim tortor, non bibendum mi varius tincidunt. Suspendisse tincidunt luctus ipsum in volutpat. In hac habitasse platea dictumst. Quisque egestas purus vitae nisl imperdiet molestie. Donec malesuada lobortis nisl, sed sagittis dui consectetur nec. Nam sed lectus id odio finibus ultrices. Nullam tempus scelerisque turpis, ut vestibulum nisl venenatis eu. Nullam eget lacinia diam. Curabitur sodales interdum lacus, et tincidunt ipsum tempus eu. Proin volutpat leo at nisi commodo euismod in sit amet nunc. Duis rhoncus interdum felis, sit amet dignissim magna rhoncus finibus. Nunc sed nibh efficitur, pharetra ex sit amet, tristique mauris.

            Curabitur viverra risus diam, id lacinia orci posuere eu. Proin mollis dictum est, eu pulvinar erat rhoncus ut. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nullam auctor nibh sit amet lorem volutpat sagittis. Vestibulum vitae auctor lectus, non pharetra orci. Donec non mi vehicula, tristique metus et, pharetra dolor. Praesent in consequat metus. Vivamus vel elementum erat, id sagittis ante. Aenean eget nunc sit amet justo faucibus ultricies. Aenean ut dolor arcu. Vestibulum eleifend sed velit eget faucibus. Donec facilisis, nisi et rutrum placerat, massa est mattis turpis, in pellentesque purus arcu semper ex. Suspendisse egestas mauris risus, eu blandit sapien dapibus et. Nulla viverra ipsum justo, nec tristique purus ultrices sit amet. Curabitur dapibus urna ut purus consectetur posuere.

            Sed laoreet sapien eget tortor suscipit ullamcorper. Morbi pellentesque eros justo, eget molestie leo hendrerit vel. Fusce ullamcorper leo mauris, et mollis ex tincidunt interdum. Praesent eu eros arcu. Integer dignissim mauris in dui interdum congue. Nulla aliquet turpis non erat scelerisque consequat. Donec at orci non sapien malesuada pulvinar eget quis leo. Aenean rhoncus leo non eros scelerisque tempus. Mauris id lorem eros. Vestibulum sit amet justo suscipit, fringilla augue vestibulum, mattis odio.

            Maecenas interdum in odio at efficitur. Interdum et malesuada fames ac ante ipsum primis in faucibus. Pellentesque nec auctor ipsum. Sed molestie molestie velit quis luctus. Nam cursus lorem consectetur ante pharetra, sed aliquam diam vestibulum. Curabitur suscipit at ipsum vitae cursus. Donec ultrices magna elit, eget tincidunt nisi suscipit sed.

            Etiam rutrum ex justo, interdum ultrices nisi interdum ut. Curabitur et lorem velit. Suspendisse ultrices tortor a ipsum imperdiet, sit amet lobortis lacus pellentesque. Nunc fringilla mattis est vel tincidunt. Aliquam erat volutpat. Fusce quis egestas velit. Curabitur pretium velit et odio gravida, rhoncus viverra tortor malesuada. Curabitur at dictum odio, nec pretium erat. Sed sed vehicula magna, sit amet placerat erat.

            Duis nec volutpat ipsum. Vivamus sit amet felis aliquam libero tempus ultrices. Nulla ac placerat velit. Duis convallis ex et leo dictum, ut dictum nulla pharetra. Morbi vitae nisi quis nisi tempus efficitur quis ut mauris. Nunc quis molestie felis. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.

            Sed libero felis, vehicula a sapien aliquet, convallis tempor dui. Morbi imperdiet neque dolor, id rutrum nulla sodales pharetra. Aenean faucibus erat massa, et pharetra felis elementum a. Aliquam vestibulum pharetra metus, quis lacinia diam imperdiet non. Quisque lacinia, nisl non placerat posuere, mauris enim posuere tortor, mollis aliquam erat nisl sit amet ligula. Sed porttitor elementum augue vel tempor. Pellentesque sagittis sem quis eleifend cursus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Aenean fringilla eros dui, in ullamcorper felis convallis nec. Ut consectetur nec leo quis semper. Etiam sapien est, tempor porta risus eu, varius ornare massa. Proin dolor diam, ornare at turpis a, sagittis porttitor libero. Donec maximus ligula erat, a vehicula ante ornare id.

            Nam at nunc urna. Quisque convallis gravida convallis. Mauris sed cursus orci. Nulla hendrerit sem a sapien porttitor ultrices. Pellentesque semper, enim vitae facilisis pharetra, dui augue laoreet magna, ac laoreet nisi est in lectus. In urna felis, semper non luctus vel, feugiat id leo. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Cras efficitur massa et neque pulvinar, in ullamcorper risus feugiat.

            Vivamus tempus neque ac dapibus sodales. Nunc id est tincidunt, pharetra diam feugiat, bibendum lorem. Maecenas dignissim tristique efficitur. Vestibulum eu nulla a tellus hendrerit lobortis id ac orci. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nullam ultricies viverra rhoncus. Integer lorem metus, pulvinar ac faucibus sed, posuere faucibus tortor. Proin purus metus, sagittis in sodales vel, efficitur sed sapien. Maecenas non congue nunc, sed efficitur massa. Vivamus finibus vel ante nec dapibus. Morbi gravida augue quis varius imperdiet. Nulla vitae lorem feugiat, laoreet augue eu, sodales enim.

            Phasellus at pulvinar ante. Phasellus non massa mollis, consectetur mauris vel, interdum lacus. Etiam hendrerit nunc iaculis elit aliquet, sed mollis eros mattis. Cras suscipit, mi et vestibulum posuere, augue est pharetra nibh, accumsan aliquam neque libero nec velit. Morbi et congue nisl. Etiam ex mi, fermentum sed vehicula sed, laoreet vel urna. Quisque elementum, metus euismod vestibulum gravida, purus ipsum molestie arcu, malesuada vulputate arcu ipsum sit amet lacus. Etiam vitae dignissim velit. Fusce egestas lacus eget magna rhoncus fermentum. Duis egestas sagittis tortor. Suspendisse consequat porta magna vitae imperdiet. Praesent massa metus, tempus sed suscipit a, fermentum in eros. Fusce sapien turpis, finibus a nulla vel, elementum egestas nunc. Etiam lobortis posuere magna sit amet pulvinar. Maecenas egestas eros nisl, quis accumsan enim varius id.

            Praesent id dapibus metus. Suspendisse potenti. Suspendisse bibendum fermentum arcu, et tincidunt turpis porttitor vel. Phasellus quis magna congue, convallis risus ac, feugiat arcu. In et turpis et libero ultricies convallis. Proin mollis aliquam ex, a volutpat erat suscipit vitae. Nam laoreet, turpis id commodo elementum, eros diam sodales sem, laoreet consectetur odio mi eget ligula. Morbi eu sem et nibh ornare hendrerit. Etiam volutpat dui sit amet tortor eleifend dictum. Sed posuere, eros quis bibendum placerat, dolor metus viverra erat, ut pulvinar dui urna quis diam. Etiam varius eros eu nisi placerat convallis. Vivamus eget felis ut leo iaculis imperdiet. Quisque id felis libero. Donec non sapien nibh. Pellentesque vel iaculis odio, ut feugiat libero. Donec venenatis turpis neque, in malesuada metus consequat ac.

            Pellentesque blandit at magna at fermentum. Morbi venenatis, felis quis sodales dictum, eros orci ullamcorper nisl, porttitor bibendum sem ipsum quis arcu. Ut ac risus dolor. Aenean semper augue nec mi gravida maximus. Quisque accumsan enim felis. Curabitur mollis, turpis nec mattis pretium, risus ante porta sem, quis eleifend magna sem sagittis ex. Aenean eu lectus sit amet ex dignissim ultricies id efficitur est. Vivamus vestibulum lorem sit amet sem euismod, a finibus lectus eleifend.

            Vivamus quis justo odio. Phasellus suscipit justo et nulla vehicula consequat. Duis viverra quis nisl sit amet volutpat. Nam blandit, eros ac euismod vulputate, nibh nunc scelerisque orci, et ultrices risus metus ut urna. In venenatis pulvinar lacus, non dictum sem vulputate non. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Vivamus id hendrerit ligula. Suspendisse mattis consectetur egestas. Curabitur at pharetra felis. Morbi vehicula, mi ac faucibus tristique, arcu leo accumsan lacus, eu elementum mi quam vel nibh. Nunc in mauris scelerisque, mollis purus vitae, vestibulum ex.

            Donec quis dui ut lacus malesuada pellentesque ut id ex. Suspendisse condimentum non enim sed hendrerit. Donec hendrerit scelerisque posuere. Nam facilisis viverra lacus, sed varius augue ornare facilisis. Vivamus at gravida enim. Fusce erat dui, porttitor a magna porttitor, interdum imperdiet mauris. Donec ullamcorper nibh iaculis, sagittis felis eu, malesuada nisi. Integer eu ex tincidunt, pharetra sem sed, eleifend felis. Ut tellus sem, maximus vel dignissim ac, pulvinar sit amet est. Aenean vel imperdiet augue, id varius quam. Nulla gravida sed nunc vitae molestie. In volutpat molestie elit, quis volutpat nisl elementum in. Nulla at ante sit amet lorem euismod finibus. Cras mattis, diam eu tincidunt condimentum, nunc sem consequat augue, vel convallis diam enim fermentum tortor. Cras dapibus faucibus neque et rhoncus. Vivamus facilisis felis id libero tincidunt convallis sodales lobortis felis.

            Vivamus ex orci, vestibulum eu leo a, mollis venenatis nulla. Ut accumsan feugiat augue, ut pretium urna auctor euismod. Nam aliquet eros sed mi cursus vulputate. Donec vulputate justo eget ultricies convallis. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam elementum viverra fermentum. Vestibulum odio tellus, vehicula sit amet tristique non, tempor non nulla. Nunc facilisis ipsum sit amet neque tempor, et vehicula erat pellentesque. Aliquam erat volutpat. Phasellus aliquam erat eget mi laoreet viverra. Nam mollis dignissim tincidunt. Sed sed justo pretium, vulputate erat ut, tristique risus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Suspendisse in elementum ante, sed mattis dolor. Quisque vestibulum dolor ut vestibulum mollis.

            Aenean vel justo aliquam, viverra tortor at, lacinia leo. Ut vitae quam mi. Donec auctor libero sit amet auctor porta. Nam nunc lorem, posuere vitae urna ut, imperdiet condimentum purus. In hac habitasse platea dictumst. Morbi pulvinar ullamcorper enim dapibus consequat. Aliquam et egestas felis, in viverra mauris. Vivamus eget turpis nibh. Nulla suscipit nulla cursus velit pharetra imperdiet. Sed sit amet faucibus magna, et gravida neque. Suspendisse eleifend dapibus lectus, ac lobortis augue scelerisque a. Integer pulvinar ligula sit amet auctor feugiat. Suspendisse potenti. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Ut rhoncus est quam, ac tempus ipsum viverra id. Nulla tincidunt, erat at vehicula sodales, elit justo hendrerit turpis, et condimentum metus libero vel massa.

            Maecenas et turpis vitae mi efficitur consectetur sed non tortor. Mauris eros tortor, iaculis et tempus vel, tincidunt vel enim. Cras lectus leo, hendrerit eget ligula eu, ultrices commodo tortor. Sed imperdiet vulputate vestibulum. Sed sed velit lorem. Donec ac condimentum metus. Etiam sollicitudin, lacus at tempor venenatis, nulla nisl mattis neque, id ultricies tortor dui non felis. Quisque pulvinar lacus quis ex fringilla facilisis. Praesent vulputate dui ac hendrerit tempus. Vestibulum placerat nisi et justo ultrices feugiat. Cras molestie quam mauris, sit amet efficitur velit consectetur id. Cras sed tellus at lacus ultrices aliquam in eu dui. Vivamus non dui id erat finibus ultrices id sed metus. Curabitur viverra elit in lorem tempor tristique. Nullam turpis libero, viverra sit amet ultricies nec, egestas sit amet urna. Sed ac dui sit amet urna accumsan ultrices vel eu metus.

            Vivamus elementum ante fermentum est aliquam facilisis. Maecenas tempus id diam faucibus consequat. Suspendisse potenti. Phasellus lectus dolor, iaculis in porttitor vel, ultrices id neque. Nulla quis ligula eget lectus commodo egestas. Fusce vulputate sapien nec auctor convallis. Aenean in purus sit amet nisl faucibus mollis. Donec orci lorem, convallis in nisi sit amet, varius vulputate ligula.

            Quisque nec odio risus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Duis egestas iaculis odio, sit amet posuere tellus viverra et. Ut aliquam magna nec volutpat euismod. Maecenas commodo libero quis augue gravida, nec placerat risus rutrum. Sed et urna non quam finibus bibendum. Duis eget nulla faucibus, hendrerit enim fringilla, malesuada mauris. Duis tempor, dui eu iaculis vestibulum, turpis quam rutrum urna, ut volutpat sapien ante quis est.

            Aliquam arcu ligula, luctus id velit id, interdum egestas lorem. Sed mi ipsum, interdum ut gravida quis, luctus sit amet dolor. Praesent faucibus mauris nulla, vel ullamcorper dui porta pulvinar. Cras elit enim, semper vel mauris vel, rhoncus pretium felis. Morbi egestas porttitor nunc quis faucibus. Nunc id tortor et nisl tincidunt sagittis. Quisque nisl orci, fermentum ut porttitor et, consectetur et augue. Aliquam erat volutpat. Mauris blandit sit amet enim non interdum. Etiam velit lectus, bibendum eget massa vel, aliquam auctor turpis. Quisque et massa ut metus pretium accumsan. Suspendisse potenti. Pellentesque sodales semper odio eu maximus. Morbi ut libero odio. Etiam non dui consequat, volutpat nibh eget, venenatis libero. Cras a cursus nisi, at fringilla ligula.

            Donec elementum tortor nisi, ac facilisis ligula scelerisque eu. In id mauris nec felis aliquam pulvinar et et ligula. Donec pellentesque metus elit, sed egestas massa vestibulum in. Nullam nunc metus, rutrum et ante ut, congue dapibus orci. Vivamus quam neque, placerat in sapien a, rutrum venenatis nisl. Proin tincidunt, erat ac venenatis lobortis, ex turpis sollicitudin dolor, vitae accumsan libero justo ac dui. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Vestibulum venenatis quam sit amet odio consectetur semper in ac eros. Fusce sollicitudin faucibus felis, vitae feugiat enim. Duis nec finibus felis, eget cursus elit. Nam enim diam, ultricies sit amet tristique eget, efficitur at eros.

            Vivamus porta maximus hendrerit. Nulla pellentesque, tortor ut vulputate finibus, urna lacus placerat ante, sed mollis lorem nibh at mi. Donec at arcu in sapien mollis luctus non in neque. Nunc tempus pharetra sapien at lacinia. In nisi mauris, posuere sit amet suscipit nec, vulputate ut lorem. Etiam in ligula a justo consequat volutpat. Cras at mollis quam. Integer fringilla blandit auctor. Maecenas finibus quis nibh a tempus. Nam convallis fringilla quam non mattis.

            In cursus urna in turpis feugiat fringilla. Pellentesque eu lectus ut metus blandit feugiat. Pellentesque vitae tempus turpis. Fusce bibendum sem sit amet justo semper sagittis. Suspendisse potenti. Nulla luctus enim condimentum, venenatis est quis, aliquet quam. Praesent et nulla eget lorem dignissim consequat non non odio. Suspendisse semper nisi quis quam facilisis, vel tempor quam scelerisque.

            Ut nec ex convallis, fringilla nulla in, ultricies nunc. Praesent fermentum, quam eu euismod rhoncus, justo libero ultricies quam, quis facilisis ex massa vel diam. Aliquam sed consectetur risus. Nam tristique nunc magna, id scelerisque ipsum tempor ut. Mauris consequat nulla non dolor rhoncus, tincidunt tempus arcu porttitor. Nunc ultricies tellus ac imperdiet porttitor. Cras nec consequat sapien, eget pellentesque enim.

            Donec ac tellus in dui rutrum rhoncus a id ligula. Ut venenatis ipsum gravida augue gravida, nec imperdiet orci pharetra. Proin dolor urna, iaculis nec leo suscipit, feugiat viverra felis. Duis ullamcorper eros maximus ante molestie, vitae vestibulum ligula feugiat. Aenean euismod vestibulum pretium. Suspendisse et euismod dolor. Integer imperdiet tempus dignissim. Aenean accumsan eros magna, ut tincidunt lacus ultricies at. Nunc id tristique mauris. Cras ac nulla lectus. Quisque fermentum est eget erat varius, at aliquet risus scelerisque. Nulla vel consectetur turpis. Pellentesque ipsum urna, egestas at lectus vel, tristique dapibus sapien. Maecenas ac urna vel sem ultrices blandit sed vel ex. Aenean ornare commodo urna sit amet ullamcorper.

            Vivamus ultrices risus mi, nec eleifend velit iaculis in. Nullam molestie nulla non sem molestie vehicula. Proin rutrum nec erat quis commodo. Interdum et malesuada fames ac ante ipsum primis in faucibus. Proin id dui mi. Mauris rutrum risus at erat facilisis aliquet. Vestibulum elementum erat sit amet metus laoreet, a luctus lacus sagittis. Aliquam tempus ullamcorper ligula, sit amet tincidunt ante pellentesque quis. Nunc consectetur ante ut elementum feugiat. Curabitur imperdiet interdum augue, vel volutpat elit hendrerit a. Vestibulum turpis sem, tincidunt id sapien et, scelerisque facilisis metus. Sed quis eros non elit tempus placerat. Nam iaculis eget risus non pellentesque. Maecenas pellentesque porttitor sem, non facilisis arcu venenatis eget. Donec eu pretium elit, id interdum dui.

            Pellentesque euismod suscipit pretium. Suspendisse ipsum felis, laoreet vitae sodales quis, mattis eget magna. Vestibulum ut augue facilisis, rutrum lacus et, aliquam sapien. Duis nec est id sem semper laoreet nec a lacus. Aliquam metus enim, facilisis a velit ac, suscipit gravida massa. Suspendisse ut diam a tellus gravida dignissim. Nulla facilisi. Morbi ornare risus lorem. Mauris in placerat augue, a suscipit nisl. Maecenas non magna egestas, laoreet enim sit amet, scelerisque purus. Donec imperdiet mollis turpis, vitae lacinia ante cursus cursus. Nam tristique lorem ac nulla accumsan ultrices. Mauris ultricies lorem laoreet, tristique dui a, faucibus diam.

            Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Duis in placerat nunc, ac semper ipsum. Integer purus libero, aliquet nec est at, aliquet dignissim eros. Fusce non imperdiet mi. Sed interdum odio vitae nunc porttitor maximus. Proin ac justo dui. Etiam sed venenatis nisl. Nam interdum mauris varius, ultrices lorem ac, luctus mauris. Nunc ac tincidunt purus. Ut aliquam ligula ac tristique malesuada. Pellentesque ornare nulla ligula, non sodales tortor luctus ut. Ut ultrices tellus vitae felis dignissim hendrerit. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; In in nulla pellentesque, dignissim lectus et, convallis arcu. In velit massa, posuere vel mauris et, tincidunt ornare diam. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.

            Donec facilisis leo sed purus vehicula accumsan. Curabitur tempus commodo neque, ut malesuada orci maximus at. Quisque tellus sem, consequat quis vehicula id, pretium lacinia diam. Suspendisse fringilla sollicitudin velit, vitae tristique augue rutrum in. Integer eu pulvinar mauris, sed mollis tellus. Quisque nec efficitur urna, ut congue sapien. Sed sed mollis dui.

            Sed luctus ex at ipsum pharetra, ac rutrum nibh scelerisque. Aliquam erat volutpat. Donec fringilla, purus quis eleifend elementum, ante est eleifend nibh, at rutrum purus odio non mi. Maecenas eleifend enim sit amet lacus fermentum posuere. Curabitur a mi maximus, ullamcorper arcu et, aliquet enim. Mauris ullamcorper sem vulputate elit sodales congue. Nulla convallis nibh nec interdum rutrum. Curabitur ullamcorper diam in commodo efficitur. Duis vitae dui molestie, auctor urna ut, maximus ex. Morbi neque neque, euismod id nisi ut, imperdiet lacinia sem. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.

            Interdum et malesuada fames ac ante ipsum primis in faucibus. Quisque egestas laoreet tortor, commodo hendrerit nunc pellentesque id. In pulvinar auctor neque, non pellentesque nunc pellentesque semper. Proin leo nisi, placerat eu diam sed, porttitor maximus metus. Praesent finibus, dui eget faucibus maximus, ex sem aliquam dolor, eu volutpat metus nibh non libero. Phasellus sit amet mi ac erat commodo condimentum. Quisque erat magna, fringilla eget nulla ac, fringilla ultrices erat. Sed varius enim tortor, vel elementum tortor volutpat eget. Nulla enim leo, sodales placerat ante ac, cursus lacinia ligula. Praesent felis ipsum, imperdiet sed facilisis eu, dictum id velit. Mauris id euismod odio.

            Integer tincidunt ipsum vel enim porta, sed faucibus turpis gravida. Integer vel sem sed leo eleifend malesuada. Morbi purus ipsum, vehicula eu diam a, euismod congue tortor. Sed faucibus nulla quis quam mollis, feugiat suscipit dui cursus. Donec vitae magna metus. Pellentesque lacinia tempus augue eget facilisis. Nam suscipit lobortis leo non dictum.

            Maecenas nibh metus, aliquet vitae facilisis sed, aliquet in tortor. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nam eu quam purus. Sed sed felis condimentum, faucibus urna quis, cursus est. Integer feugiat mollis turpis, in dictum risus convallis id. Morbi viverra enim ac lectus ultricies gravida. Pellentesque id libero mauris. Cras eleifend massa mauris. Mauris euismod leo dolor, et congue est venenatis et. Fusce porttitor gravida nunc et venenatis. In bibendum augue at sem dignissim viverra. Sed malesuada venenatis lacinia. Nullam gravida metus ac faucibus faucibus.

            Nullam id quam sed ex pharetra sodales. Suspendisse tincidunt, justo non accumsan laoreet, risus metus tincidunt tortor, non commodo orci risus sit amet ex. Nam facilisis semper sodales. Fusce eleifend ullamcorper congue. Vivamus vitae massa a neque ullamcorper molestie. Curabitur justo enim, lacinia id tincidunt a, consequat ac sem. Nam leo eros, scelerisque in velit nec, sagittis fringilla metus. Morbi fringilla facilisis est, eu venenatis lacus scelerisque sed. Vestibulum vitae odio tempus, efficitur nunc et, accumsan leo. Suspendisse potenti. Morbi id eros arcu. Suspendisse vel libero ligula.

            Cras feugiat tincidunt porttitor. Suspendisse nunc velit, ultrices vel iaculis ut, blandit sit amet leo. Curabitur vitae pretium turpis. Fusce libero ipsum, elementum ut maximus id, placerat eu lacus. Phasellus suscipit ut turpis vitae malesuada. Etiam elit nisi, cursus vel facilisis sed, feugiat sed tellus. Sed suscipit vel neque id porta. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Aliquam ac consequat metus. Sed id suscipit magna. Donec sit amet arcu nec leo dictum gravida. Etiam a arcu condimentum, tristique dolor ac, varius lectus. In lobortis rutrum risus.

            Nunc et urna nunc. In sed ornare elit. Maecenas scelerisque blandit velit, laoreet hendrerit nibh mattis eu. Sed ut suscipit lorem, sed pellentesque sapien. Ut rhoncus congue neque, ut porta nisi placerat eget. Curabitur non mi porttitor, rutrum ligula ac, facilisis augue. Suspendisse maximus metus quis quam fermentum viverra. Sed iaculis scelerisque lorem, sed ullamcorper dolor tristique ac.

            Vivamus tincidunt egestas nunc vel dignissim. Maecenas sit amet metus a ante mattis commodo. In elit purus, sodales a aliquet ut, aliquet id lacus. Curabitur posuere malesuada leo, eu consequat elit ultrices a. Vivamus quis velit ac velit condimentum tincidunt vitae quis velit. Proin eleifend rutrum eleifend. Donec sit amet metus at arcu ultrices vehicula. Nulla imperdiet quam ut gravida tempus. Sed et nisl fringilla, efficitur nibh vel, hendrerit libero. Integer vitae ante purus. Donec iaculis tortor a eros volutpat mollis. Proin non magna ut quam elementum faucibus vel sed eros. Nam et sapien ex.

            Pellentesque eget mauris eget risus varius vehicula. Fusce nec vehicula purus. Morbi vel sapien porta, lobortis quam quis, posuere urna. Donec id auctor leo. Ut ipsum elit, maximus luctus libero vel, consectetur gravida enim. Integer viverra vulputate tortor, accumsan sodales risus mollis non. Duis vel velit nec turpis molestie finibus eu at ligula. Donec tristique sed mauris a ullamcorper. Sed a venenatis nulla, et elementum dolor. Nulla vitae enim ipsum. Vestibulum gravida sapien nec nisl mattis sollicitudin. Morbi hendrerit luctus sem nec lacinia. Curabitur dapibus faucibus dui, non ultrices odio fringilla in.

            Suspendisse accumsan dui sagittis fermentum accumsan. Ut placerat imperdiet erat gravida tincidunt. Donec fermentum porta turpis eu gravida. Nullam finibus neque ex, in mattis nibh viverra at. Phasellus vitae vulputate magna. Vestibulum eu aliquet magna. Ut fringilla, sem at suscipit suscipit, sem metus scelerisque leo, quis egestas diam tellus in arcu.

            Nunc vitae porttitor urna. Cras ut turpis laoreet, blandit eros eu, aliquet purus. Donec iaculis arcu a finibus gravida. Praesent et porttitor erat. Duis consequat ante sit amet blandit viverra. Nulla facilisi. Integer in orci vel lacus congue consequat et eu sapien. Aliquam consectetur libero sapien, eget molestie neque tristique quis. Vivamus consectetur nulla ut dui laoreet porttitor. Sed est enim, fringilla imperdiet aliquam et, sodales in ante. Suspendisse iaculis ut lacus nec pretium. Integer varius nisl elementum tellus dignissim pellentesque. Donec vitae porttitor ante. Donec eget finibus turpis, ac convallis est. Aliquam velit lacus, interdum feugiat tincidunt nec, venenatis a magna.

            Nam vitae hendrerit nibh. Vestibulum lacinia, arcu in bibendum vulputate, orci arcu rhoncus turpis, non interdum tellus purus fermentum purus. Ut euismod vehicula eros, quis laoreet sem euismod quis. Fusce tempor sed nunc tincidunt ultrices. Integer dapibus velit pharetra elit rutrum, nec sodales neque pulvinar. In quis interdum quam. Pellentesque volutpat arcu vitae est iaculis finibus. Aliquam erat volutpat. Nullam sed felis rhoncus, auctor purus placerat, porttitor diam. Nam sit amet rhoncus ipsum.

            Curabitur faucibus condimentum egestas. Aliquam luctus, mi at commodo tincidunt, elit risus congue mi, sit amet pretium est felis eu turpis. Ut fringilla, libero a congue eleifend, nisl massa pretium leo, at lacinia mauris massa at sem. Nam eget est magna. Sed maximus tempor elit non rhoncus. Vivamus lobortis mauris ipsum, a iaculis turpis feugiat at. Integer mattis neque eget nunc mattis pulvinar. Mauris velit odio, molestie vitae consequat sit amet, dapibus in arcu. Nunc dignissim, libero at laoreet elementum, nibh nibh viverra urna, eu placerat justo magna id leo. Nullam eget lacinia dolor.

            Suspendisse feugiat porttitor urna, ac feugiat mauris. Integer in erat pulvinar, fringilla velit ac, suscipit sem. Vestibulum ac suscipit quam. Nam eget fermentum massa. Donec tristique ultrices metus, bibendum condimentum nisi varius id. Vivamus in rhoncus diam. Ut eget ultrices neque. Vestibulum consectetur libero sit amet ultrices suscipit. Donec vel tortor luctus, elementum nulla id, finibus erat. Curabitur eu feugiat dolor, nec mattis eros.

            Cras nisl purus, scelerisque nec pretium in, suscipit in ligula. In at felis tincidunt, convallis sapien eget, convallis nibh. Phasellus erat lorem, porttitor eget sapien eu, pretium posuere turpis. Proin non hendrerit mi. Donec sed cursus leo, quis congue arcu. Nulla lacinia lacus non tortor ultrices consequat. Nam aliquet, ex quis pretium euismod, est turpis porttitor sem, ac pretium magna odio vel lorem. Suspendisse ac cursus nunc. Sed congue enim sit amet ipsum consequat, non sodales nulla pulvinar. Morbi sem quam, lacinia fringilla pulvinar sed, bibendum eu nunc. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Quisque ultrices, risus ac hendrerit gravida, ligula enim laoreet enim, nec sodales nulla risus ac urna. Ut bibendum mauris eu purus sodales aliquam. Aliquam facilisis urna sit amet erat scelerisque, eget fringilla massa ultricies. Fusce pretium lacus at lobortis facilisis.

            Duis scelerisque sollicitudin mauris sit amet aliquet. Aenean massa justo, lobortis at condimentum ut, efficitur eget enim. Praesent dictum lectus ut felis molestie, gravida convallis est ullamcorper. Suspendisse commodo semper efficitur. Ut eu laoreet quam. Nullam id odio tincidunt, lacinia neque in, consequat ligula. Quisque sit amet arcu lacinia metus fermentum facilisis. Nunc hendrerit vitae quam et consectetur. Pellentesque commodo egestas pulvinar.

            Nulla facilisi. Sed dignissim luctus urna eu lacinia. Etiam mattis justo pretium pulvinar bibendum. Mauris volutpat posuere tortor, vitae auctor arcu hendrerit ac. Proin ornare volutpat sem, at posuere lorem interdum non. Maecenas elit est, vestibulum tristique lacinia in, imperdiet quis erat. Mauris vitae ultricies nulla. Aenean et urna aliquet, viverra massa ac, euismod nunc. Sed aliquet dignissim eros et bibendum. Vestibulum sagittis vehicula ante sed sollicitudin. Quisque dapibus eu eros et vestibulum. Nullam at nibh ac leo luctus volutpat. In finibus condimentum nulla nec euismod. In cursus dignissim bibendum.

            Cras vulputate quam in ultrices eleifend. Donec metus dui, dignissim sit amet risus eget, finibus condimentum nulla. Aliquam ut est vulputate, auctor lectus ac, pharetra sapien. Maecenas egestas, dui sit amet fringilla molestie, purus libero blandit ipsum, et convallis diam ligula a turpis. Mauris eget efficitur libero. Sed dolor erat, scelerisque in lectus nec, commodo ultricies risus. Suspendisse metus lorem, aliquam ac mollis eu, aliquam non libero. In at arcu porttitor, gravida tellus interdum, blandit nulla. In ultricies magna ac placerat hendrerit. Nullam non lacus tempus, pharetra risus vitae, efficitur augue. Aliquam efficitur, tellus sed luctus pellentesque, massa diam molestie quam, id fermentum eros lectus sit amet nisi. Curabitur diam nulla, suscipit eget ligula nec, dapibus egestas metus. In ultrices tincidunt porttitor. Etiam et tortor egestas, laoreet mauris venenatis, varius nulla. Proin sit amet sem pretium nisl pharetra viverra mattis nec urna.

            Proin sagittis eget ante vel tempus. Nulla varius libero sit amet ex ornare tristique ut eleifend enim. Etiam viverra ante enim, vel dictum odio viverra feugiat. Aliquam semper interdum nulla, ac sodales nunc porta sed. Morbi eget facilisis leo. Nulla facilisis lacinia lobortis. Vivamus pharetra ipsum metus, ac maximus neque lacinia vitae. Sed scelerisque velit enim, in vulputate eros pellentesque sit amet. Proin in iaculis metus. Donec suscipit scelerisque felis in sodales. Maecenas suscipit orci congue justo vestibulum varius. Maecenas vitae tempor massa. Etiam quis rutrum risus, at dignissim massa. Suspendisse quis lectus id odio sodales pharetra sit amet ac turpis. Proin quis suscipit ante.

            Sed commodo hendrerit augue, id aliquet ante fringilla tincidunt. Nunc accumsan maximus ultricies. Nam tincidunt eleifend turpis vel rutrum. Quisque congue enim in neque porttitor auctor. Curabitur a mollis lectus. Ut sollicitudin congue dui, nec porttitor sapien posuere at. Integer est nisi, feugiat sit amet malesuada vitae, iaculis eu sapien. Ut eu augue dui.

            Integer feugiat velit elit, eleifend sodales quam gravida vel. Ut porta eleifend tellus. Mauris accumsan enim in velit aliquam, non lacinia tellus mollis. Duis a leo et elit mattis bibendum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Vestibulum nec metus vitae magna pellentesque bibendum. Quisque eget nulla in nisl sollicitudin dapibus quis eu orci. Curabitur viverra fermentum magna, vel pharetra arcu iaculis eu. Praesent id commodo tellus, nec congue nibh.

            Praesent a urna vitae urna lobortis malesuada. Duis quam arcu, volutpat non diam et, vulputate viverra lorem. Nullam vitae tortor vitae nibh feugiat dignissim vel eget odio. Sed semper condimentum pellentesque. Nunc malesuada ligula sit amet bibendum consectetur. Praesent lacinia faucibus velit, vel tincidunt est pellentesque sit amet. Phasellus consequat lectus at mauris cursus, sed fermentum nisi feugiat. Ut sit amet neque eget purus dapibus imperdiet nec sed est. Nullam sed risus non eros bibendum tempor. Fusce nec nulla eu nisl varius lacinia. Nam lobortis eros nec tortor ullamcorper blandit. Donec ultrices quis dui sit amet interdum. Praesent consectetur sed arcu eu dictum. Vestibulum sed diam ut urna molestie malesuada. Pellentesque elit orci, auctor pellentesque aliquet a, efficitur a justo. Duis lobortis vitae eros quis tincidunt.

            Pellentesque at fermentum felis. Morbi eleifend facilisis est, a lacinia risus pharetra a. Aliquam sit amet quam erat. Maecenas pretium, orci vitae efficitur rutrum, augue magna cursus massa, vel hendrerit turpis risus sed risus. Suspendisse feugiat gravida nibh id hendrerit. Interdum et malesuada fames ac ante ipsum primis in faucibus. Donec eget auctor massa.

            Ut sit amet erat non urna laoreet congue. Ut eleifend ac quam finibus consequat. Aliquam erat volutpat. Donec ut lacinia urna. Vestibulum finibus pretium nunc, eu eleifend metus sodales in. Aenean tristique tortor nec nisl volutpat, non pharetra mi posuere. Proin in blandit ligula. Cras ut dolor at neque aliquet mattis. Mauris at mattis nibh. Curabitur pulvinar diam eu velit tristique varius. Maecenas ante mi, venenatis ut condimentum quis, tincidunt eget nunc. Quisque neque neque, dignissim non est vel, cursus laoreet arcu. Proin nec eros sed enim rhoncus aliquam vitae ut orci. Mauris posuere erat non mauris cursus, sit amet suscipit metus fermentum. Curabitur in turpis vestibulum, lacinia lectus eu, tempus erat. Aliquam erat volutpat.

            Mauris consectetur hendrerit ligula, vel viverra augue placerat a. Donec convallis, lectus mattis volutpat feugiat, ipsum lectus rhoncus erat, ut fermentum felis ante a tortor. In in ex ultrices, fermentum metus in, posuere metus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla sollicitudin neque in mauris accumsan placerat. Aliquam placerat finibus odio ut luctus. Etiam sit amet lobortis arcu. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi ac ornare sem. Nunc ornare bibendum neque. Ut hendrerit iaculis dictum. Integer lacinia, lacus et ullamcorper iaculis, odio odio maximus quam, vitae aliquam lacus nisl non augue. Suspendisse vestibulum, nibh eu rhoncus ornare, purus metus iaculis libero, ac posuere quam ligula a augue. Ut dignissim lectus suscipit molestie interdum. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent sit amet fermentum massa.

            Ut egestas, ligula ornare hendrerit iaculis, urna urna pulvinar quam, vel volutpat leo velit vitae ligula. Aliquam aliquam nibh a augue dapibus placerat. Nunc ac nisl a diam varius dictum. Nullam finibus dapibus justo, ut mollis ligula auctor ac. Vestibulum tincidunt neque sit amet nisi commodo, id cursus risus dictum. In sit amet facilisis nisi. Donec semper malesuada orci, non vehicula nibh eleifend eget. Quisque auctor, neque et auctor ultricies, mauris erat volutpat nibh, id efficitur dui orci a erat. Suspendisse potenti. Nunc aliquam magna ut odio auctor aliquam. Maecenas id finibus sapien. Nullam varius, dui nec elementum tristique, leo sapien consectetur nisl, vitae auctor massa tortor ut ex. Aliquam at posuere erat. Donec et erat pellentesque, tristique dolor nec, pharetra elit.

            Suspendisse potenti. Aliquam erat volutpat. Quisque nibh quam, pretium eget aliquam et, porta laoreet diam. Quisque eleifend quam vitae erat imperdiet pretium. Cras sed magna eu augue placerat aliquam at ut enim. Nam dui ligula, mattis porta viverra nec, gravida at ipsum. Phasellus tempor laoreet aliquam. Etiam non elementum mi. Phasellus augue erat, molestie nec dolor eu, sodales posuere dui. Quisque ut finibus magna.

            Morbi tristique, est et posuere bibendum, augue eros cursus metus, eu imperdiet leo justo a dui. Praesent lobortis lacus purus, tempor accumsan nisi porta non. Sed tincidunt, urna nec hendrerit sollicitudin, sapien quam tempor diam, vel fermentum neque tortor et lorem. Donec tristique leo sit amet arcu porttitor dignissim. Fusce nec dolor et lacus maximus tincidunt. Fusce aliquam nulla nec neque tempor facilisis. Aenean vestibulum congue tellus eget cursus. Cras ut mauris lectus. Morbi ornare viverra dui, vitae accumsan dui euismod vitae.

            Nam magna dolor, ullamcorper non sollicitudin at, lacinia quis mauris. Aliquam neque ex, volutpat vel magna sit amet, pharetra rutrum orci. Suspendisse tempor blandit urna, sit amet malesuada nulla porttitor at. Aenean rhoncus mi a egestas laoreet. Mauris quis varius urna, sed tincidunt diam. Morbi efficitur ornare consequat. Vivamus quis diam placerat, pulvinar lectus et, pellentesque orci. Curabitur venenatis efficitur diam, auctor vulputate tellus dictum at. Nulla elementum, lorem eget efficitur porttitor, mi enim elementum neque, a dapibus purus tortor id eros. Duis sit amet tincidunt lorem. Etiam vehicula ultrices pretium. Nunc vestibulum purus in velit viverra, et scelerisque mauris lobortis. Interdum et malesuada fames ac ante ipsum primis in faucibus. Proin lectus arcu, lacinia ac eleifend id, lobortis sed orci. Quisque non quam tellus.

            Suspendisse pharetra sem sit amet felis vehicula rhoncus. Donec vitae tempor nunc. Praesent mollis diam vitae nulla vestibulum, non vestibulum turpis congue. Cras interdum justo at neque euismod euismod. Quisque ut risus vulputate, placerat tellus sit amet, laoreet velit. Nulla facilisi. Ut mi ante, vehicula vitae faucibus sed, convallis sed neque. Aenean molestie, sem sed vestibulum imperdiet, nibh leo tempor velit, non auctor arcu risus molestie neque. Donec eget mauris accumsan magna eleifend feugiat sed ac orci.

            Morbi vel orci eget lacus accumsan consequat. Mauris ultrices, eros eu varius vestibulum, risus mauris feugiat nisl, at commodo lectus urna eget justo. Duis fringilla, justo a rutrum fermentum, diam nisl semper augue, eu facilisis justo libero pretium arcu. Donec tincidunt iaculis vestibulum. Praesent hendrerit efficitur dui, eu ultrices neque faucibus ut. Praesent ex risus, consectetur non nulla a, tempor feugiat mauris. Aenean bibendum quam consectetur felis ultrices, et varius purus consequat. Morbi tellus lorem, congue mattis lacus et, convallis commodo libero. Nunc congue lacus porttitor, semper ipsum quis, suscipit ante. Sed at neque non massa molestie pharetra finibus at mauris. Sed blandit ultrices dolor, ut auctor urna porttitor quis. Sed pulvinar faucibus lorem eu venenatis. Curabitur nec magna sed dolor egestas laoreet at nec lectus. Suspendisse ullamcorper dictum augue at efficitur. Aliquam vel tincidunt diam.

            Nam et massa mi. Donec blandit, leo in scelerisque efficitur, lacus metus euismod tellus, at mattis eros felis quis ante. Morbi magna diam, fringilla non vestibulum nec, mollis non ipsum. Morbi sit amet urna quis lacus semper tempor sed nec lacus. Aenean convallis mollis enim, nec condimentum libero mattis eget. Integer pellentesque sit amet arcu eu molestie. Suspendisse felis elit, blandit eget risus ac, vehicula laoreet leo. Integer sed massa id elit cursus dignissim. Vivamus malesuada aliquam risus, id efficitur tellus fringilla non. Morbi quis neque ac purus vestibulum eleifend. Aliquam tempus nunc ac interdum semper.

            Aenean sit amet consequat urna. Vestibulum pretium molestie tortor, eget congue lacus volutpat eget. Cras tempus venenatis mi, nec facilisis velit elementum vel. Vestibulum vel posuere magna. Nullam nec elit diam. Nulla malesuada tellus nec felis aliquet ullamcorper. Donec vulputate enim at felis tincidunt elementum. Nunc volutpat consequat odio, a suscipit tellus faucibus a. Duis lacinia vulputate pulvinar. Pellentesque porttitor massa ac nisl tincidunt condimentum. Donec tellus lorem, rutrum id maximus sit amet, fringilla sit amet est. Vestibulum quis dolor mattis, ultricies justo nec, vestibulum metus. Quisque volutpat metus enim, sit amet scelerisque massa interdum sit amet. Pellentesque euismod eleifend ornare. Proin et convallis justo.

            Proin ipsum magna, auctor et sodales a, semper in magna. Nunc ac magna at orci ullamcorper feugiat. Aenean sodales magna eget arcu pulvinar gravida. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi accumsan vehicula purus, id dignissim diam iaculis vitae. Nulla nibh neque, mollis gravida metus id, consectetur ultricies metus. Pellentesque blandit tincidunt lorem nec consectetur. Sed pellentesque tempor nisi quis eleifend. Pellentesque molestie nulla non accumsan rhoncus.

            Aliquam posuere faucibus augue sit amet sodales. Aliquam malesuada vehicula libero, sit amet pellentesque justo tincidunt ac. Nulla luctus finibus enim, sed feugiat dui congue nec. Curabitur purus dolor, ullamcorper sit amet lorem accumsan, bibendum pretium nulla. Mauris finibus sed nulla et fermentum. In et tellus vitae velit convallis euismod vitae a mauris. Nam cursus suscipit dui pellentesque aliquet. Donec interdum scelerisque mattis. Aliquam erat volutpat. Nam erat metus, aliquam ut egestas ut, iaculis non justo. Pellentesque gravida nisi in diam interdum, eu tincidunt metus eleifend. Etiam id dictum nulla, ac tempus sem. Nulla laoreet risus mi, quis accumsan leo porttitor in.

            Vestibulum auctor bibendum erat, et venenatis risus tempus dictum. Nullam ut sapien faucibus, ornare lorem nec, elementum odio. Donec gravida pretium leo non vestibulum. Aenean euismod rhoncus justo quis vehicula. Aliquam volutpat ultrices orci ut auctor. Praesent sed libero vel turpis molestie porttitor eu suscipit risus. Nunc ultricies elit facilisis lectus placerat, eu ornare ipsum pretium.

            Phasellus ac feugiat velit, at aliquam lacus. Vestibulum sit amet tellus mattis, ultricies augue nec, rutrum elit. Aenean sit amet gravida massa. Nam efficitur, sapien sed commodo rhoncus, neque lectus posuere urna, eu tincidunt ante nisi eu justo. Quisque et venenatis augue. Etiam nulla massa, laoreet sed sapien vel, elementum aliquet lacus. Mauris imperdiet, nisl tempor mattis commodo, ex massa semper mauris, eget placerat dolor tellus et tortor. Mauris commodo et sapien nec sodales. Curabitur quis tellus augue. Duis vitae nunc vitae purus consectetur ornare.

            Vestibulum ullamcorper nisi id faucibus ultricies. Donec hendrerit justo vitae sem pharetra mollis. Etiam non porttitor urna, id porttitor mauris. Etiam vestibulum lobortis libero, sit amet pulvinar risus lacinia nec. Mauris molestie, elit quis molestie accumsan, dolor orci varius turpis, eget varius libero velit ullamcorper nibh. Ut a felis et justo efficitur laoreet et id nunc. Aenean sodales urna sit amet est sagittis feugiat. Cras quis vehicula felis. In at elit et arcu varius cursus.

            Mauris sit amet consectetur dolor. Pellentesque rhoncus dictum maximus. Fusce et lorem tellus. Nulla rutrum convallis nisi, vitae consequat ante consequat pellentesque. Donec consectetur dui laoreet lectus imperdiet, et condimentum diam congue. In eu elit elit. Aenean a orci vel enim semper aliquam vel nec lorem.

            Aliquam posuere ipsum in sapien pellentesque congue. Morbi tortor lorem, lacinia id nisi vulputate, venenatis suscipit lectus. Donec at molestie dui. Suspendisse facilisis, quam euismod condimentum lacinia, tellus dolor vulputate erat, id scelerisque erat mauris sed ex. Aenean cursus cursus efficitur. Praesent quis erat odio. Morbi at nunc mattis, suscipit lectus sagittis, placerat velit. Duis vulputate velit arcu, id volutpat turpis ultricies quis. Cras tempor congue urna ut iaculis. Proin et libero at augue lacinia placerat. Aliquam nec erat a urna tincidunt mattis. Nam consectetur efficitur venenatis.

            Curabitur purus arcu, varius sit amet gravida vel, molestie id ex. Donec condimentum commodo libero in semper. Fusce placerat pulvinar urna, vel interdum tellus ultricies vel. Integer interdum massa magna, sed maximus metus dictum non. Etiam ac dui nisi. Proin eu facilisis felis. Nunc auctor diam non leo consectetur, varius vehicula nulla interdum. Donec libero justo, vehicula sed mauris at, semper suscipit libero. Etiam pulvinar quam nec metus pellentesque, id aliquet tellus posuere.

            Pellentesque ac turpis ut augue tristique luctus ac vel diam. Maecenas nec tristique elit, quis ornare lectus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Vivamus eget est quis nisl gravida blandit. Morbi rhoncus, nibh ac maximus semper, nulla sem ornare ligula, at consequat nisl orci id ex. Proin dui ex, suscipit non ultrices nec, bibendum et risus. Nulla facilisi. Etiam elit metus, vestibulum non augue sed, placerat laoreet lorem. Etiam a tempor orci. Donec id eros velit. Quisque sit amet enim facilisis, interdum nibh in, euismod lacus. Praesent id elit nisl. Aenean vehicula non lacus eu condimentum.

            Pellentesque quis ipsum ullamcorper, condimentum sapien eget, ornare urna. Cras vel nisl arcu. Suspendisse efficitur urna non luctus gravida. Pellentesque lacinia diam at urna blandit malesuada. Sed ut elementum justo. Integer vehicula velit mi, non congue arcu tempor ut. Sed bibendum vel tortor non ullamcorper. Sed vestibulum malesuada urna, ut sodales tortor dapibus vel.

            Suspendisse in maximus metus. Nulla eu lorem sit amet dui commodo laoreet a ac nibh. Phasellus sollicitudin congue eros, eget aliquam eros consequat pulvinar. Curabitur ultricies consectetur nulla, eget accumsan tellus sagittis et. Donec mollis vehicula nisl, vel faucibus quam. In placerat dignissim fermentum. Praesent a turpis egestas, tincidunt neque in, elementum dui. Nulla nunc leo, viverra id felis et, pretium convallis magna. Mauris vel purus ut risus scelerisque faucibus quis ac sem. Quisque dignissim mauris et efficitur fermentum. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Cras volutpat aliquet suscipit. Ut placerat, ante at mattis malesuada, tellus quam faucibus risus, eu accumsan neque erat a libero. Donec pellentesque ut lectus sit amet consequat. Vivamus sollicitudin dui vel velit pulvinar gravida.

            Aliquam tincidunt eget odio vel malesuada. Maecenas at sem ac nisi vulputate dignissim. Fusce congue mattis lectus, ut varius sapien condimentum at. Maecenas tincidunt libero elit, ut tempor odio ultricies ac. Nulla ullamcorper, mauris ac ullamcorper placerat, sem felis volutpat ipsum, consectetur pretium metus elit nec ligula. Aenean auctor lacus nisl, id commodo enim tincidunt quis. In rhoncus arcu dolor, eu varius velit porttitor non.

            Pellentesque vitae dolor fermentum, commodo felis et, dapibus massa. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Sed eu tempus lacus, eget efficitur eros. Ut at lacus at felis auctor tincidunt. In laoreet hendrerit erat. Curabitur sed dapibus mauris. Donec efficitur ligula et dolor commodo malesuada.

            Proin vitae gravida ante. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed eu imperdiet lorem. Aliquam et eros et nisi finibus cursus eu non nisi. Nullam sapien sapien, ultricies ac enim a, sodales pharetra mi. Fusce ac ex eu mi maximus tempor vel sed felis. Aenean at nisi fermentum, porta sapien in, faucibus odio. Nullam vitae libero vel quam vehicula interdum ut tristique enim. Suspendisse efficitur, odio ut scelerisque varius, urna mi facilisis felis, eget ullamcorper tellus tortor malesuada eros. Vivamus luctus elementum suscipit. Suspendisse venenatis eleifend mauris, nec blandit metus. In hac habitasse platea dictumst. Vivamus condimentum leo vitae pellentesque congue.

            Vestibulum finibus est quam, et vehicula tellus efficitur ac. Morbi ut sapien eleifend, eleifend enim efficitur, dapibus nisi. Praesent ultricies sem eros, sit amet egestas enim tincidunt sit amet. Aenean malesuada auctor felis, nec pellentesque sem maximus eget. Fusce posuere justo libero, non consequat risus faucibus in. Aliquam pulvinar ultrices aliquet. In nec varius elit. Nunc ac metus consectetur, eleifend ipsum a, scelerisque metus. Maecenas libero ligula, efficitur sit amet risus in, dictum consectetur magna. Nunc ac tincidunt orci. Praesent nulla nibh, dapibus sed tempor eget, tempor aliquet ipsum. Maecenas pretium imperdiet ante at iaculis. Curabitur mollis nibh et sapien rutrum vulputate et non quam. Duis consequat turpis dignissim, venenatis felis at, porttitor tortor. Etiam sed elit in purus sollicitudin venenatis et sed nulla. Integer imperdiet odio elit.

            Duis condimentum ultricies nunc ut efficitur. Morbi ut gravida turpis. Quisque cursus sagittis nisi, sit amet mollis enim pellentesque id. Curabitur velit mauris, elementum non porttitor eu, vulputate at dolor. Donec iaculis, lacus in aliquet ornare, erat nisi convallis elit, quis imperdiet turpis arcu id nunc. Maecenas euismod augue dignissim euismod venenatis. Ut porta velit ac eleifend lacinia. Duis facilisis non leo a pellentesque. Nunc lacinia orci sit amet ultrices suscipit. Donec consequat sapien sed cursus rhoncus. Duis consectetur eu justo at scelerisque.

            Vestibulum blandit risus vitae nibh elementum, vitae rhoncus nisl faucibus. Quisque porta nisl mauris, in pretium urna dictum at. Aliquam vel varius quam, non auctor augue. Maecenas non consectetur tellus. Aliquam pellentesque turpis et urna rhoncus, id convallis nibh ultricies. Donec rhoncus libero erat, quis commodo nisl eleifend in. Nulla massa lectus, ullamcorper sit amet mauris ut, tincidunt ornare erat. Morbi velit nunc, tincidunt non ante at, tincidunt lacinia velit. Sed vitae porttitor massa, in gravida nulla. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Duis euismod faucibus rhoncus. Sed pulvinar in leo ut pellentesque.

            Donec aliquam elit turpis. Phasellus ex ligula, vulputate quis euismod quis, imperdiet ut eros. Nulla facilisi. Nulla libero leo, mattis vitae lobortis a, tincidunt eget ex. Nunc molestie ipsum bibendum justo lobortis, vitae accumsan magna viverra. Integer et arcu ut ligula finibus pretium. Integer auctor luctus sem, et imperdiet dui ultrices vel. Donec pretium ultrices ante id sollicitudin. Fusce non mauris eu ipsum tristique ultricies. Morbi dictum, lorem laoreet suscipit vehicula, nibh ligula aliquet dui, sed lobortis augue odio sit amet diam. Maecenas et turpis tellus. Sed facilisis dui vitae rhoncus luctus. Quisque tincidunt, arcu ut congue molestie, leo nisl rhoncus diam, in dapibus metus turpis non turpis. Vestibulum non orci maximus, elementum nulla eu, egestas mi. Nulla vel dui tincidunt, pulvinar lacus eget, congue justo. Sed augue justo, dictum sed nisl tristique, aliquet scelerisque nulla.

            Cras at nunc odio. In erat felis, semper vulputate consectetur et, porttitor vel sem. Sed dictum orci ut tincidunt ultrices. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Vestibulum vehicula, dui id semper rutrum, leo nisi faucibus libero, at mollis justo orci vel odio. Mauris pellentesque dolor blandit tellus posuere semper. Vestibulum eget finibus nibh. Nam hendrerit quam velit, a sagittis justo aliquam ac. Maecenas congue at nisl nec pretium.

            Duis massa neque, auctor sit amet pretium eget, finibus id nisl. Sed sapien risus, sollicitudin vitae sodales et, facilisis a ex. Donec nibh ante, tempus sed neque eu, feugiat ultricies est. Vestibulum varius semper vulputate. Curabitur eu tincidunt enim. Aenean condimentum, eros dictum posuere placerat, ipsum tortor rutrum nisl, eget mattis metus ligula non augue. Aliquam pretium sapien non erat varius, at rutrum orci ultrices. Aenean at convallis elit. Nullam lobortis placerat magna. Nulla ac massa dapibus, congue nisl et, dapibus arcu. Morbi nec lorem maximus, hendrerit ligula ac, varius felis.

            Maecenas ac quam ac tortor congue convallis vehicula a dui. Ut scelerisque nibh vel neque vestibulum, elementum porttitor nibh ullamcorper. Proin elementum venenatis vulputate. Aenean pellentesque odio neque, vel congue ante facilisis ut. Integer cursus iaculis nibh, eget vestibulum ipsum pulvinar consequat. Ut sit amet porttitor ex, sed blandit nisl. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Proin ac sagittis nisl, vitae cursus lectus. Sed eros tortor, laoreet fermentum luctus vel, eleifend ac odio. Vestibulum pharetra odio turpis, a posuere nibh pretium in.

            Phasellus vitae nunc in massa eleifend viverra ut ac massa. Integer venenatis nisi a rhoncus porta. Etiam cursus ex in ipsum fermentum auctor. Curabitur ut ante in lorem porttitor egestas a a nunc. Mauris tellus nulla, efficitur nec est ut, condimentum maximus ipsum. Aliquam nunc nunc, iaculis at ex sagittis, pharetra elementum augue. Sed non euismod urna, vitae posuere lacus. Donec quis aliquam sem.

            Integer sed sodales nibh. Cras congue bibendum condimentum. Integer semper quam magna, quis pulvinar diam viverra vel. Quisque sit amet quam ut nisi vehicula dignissim. Maecenas nunc enim, fermentum ut ante vel, rhoncus accumsan ligula. Fusce posuere est arcu, quis elementum lacus aliquet non. Nullam in dignissim velit. In a ante arcu. Integer vel sagittis mauris, a auctor enim. Nam ut erat at dolor pellentesque tincidunt eget non nisi. Aenean et lorem dapibus, feugiat dui quis, pellentesque sapien. Sed vitae ipsum dolor. Sed sed commodo eros. Aenean sollicitudin elementum ante a auctor.

            Aliquam ac rutrum sapien. Proin interdum feugiat lacus nec elementum. Nulla luctus sapien dictum, mattis mauris facilisis, placerat ligula. Proin venenatis, orci a consequat rutrum, tortor erat dignissim urna, vitae pellentesque sem nisi vel tellus. Nam eget lobortis dolor. Nam nisl nibh, finibus id urna eu, aliquam feugiat felis. Phasellus nec auctor sem. Duis finibus risus vel tortor bibendum, ut posuere quam consequat. Duis aliquet laoreet justo vitae condimentum. Nulla tincidunt varius augue, in aliquam odio auctor sed. Maecenas consequat ultricies arcu, vitae feugiat quam sodales accumsan. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Suspendisse pulvinar mauris erat, elementum euismod dolor consectetur in. Pellentesque vel consequat lectus, in pretium massa. Pellentesque ac ultrices erat. Curabitur vitae tellus a quam hendrerit faucibus.

            Curabitur dignissim tincidunt augue sed commodo. Donec id finibus nibh. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer ut turpis at urna accumsan convallis quis eu odio. Fusce ac consectetur lacus, quis vulputate nisi. Maecenas id imperdiet magna. Duis tristique ullamcorper lectus ac tempor. Cras gravida cursus tellus, ut porta urna bibendum ac. Phasellus facilisis et orci ac tincidunt. Nam vulputate urna sed libero posuere, luctus tempor eros malesuada.

            Ut in nulla nec ipsum convallis feugiat. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Sed quis pharetra nulla, eu iaculis lectus. Duis congue ante eget neque mollis, egestas condimentum sem placerat. Vivamus euismod, felis posuere pharetra efficitur, libero dolor viverra dolor, nec lacinia nisi elit nec nisi. Aenean rhoncus sit amet nibh id posuere. Nullam libero eros, condimentum ut leo tempor, porttitor efficitur odio. Sed tempor, justo at eleifend dapibus, tortor est ultricies orci, eget feugiat dui enim ut tortor. Aliquam malesuada pellentesque mattis. Proin iaculis, massa in efficitur ornare, nisi mauris commodo nisi, nec semper nibh orci ut erat. Proin volutpat dolor sit amet nisl accumsan ultricies.

            Sed placerat placerat quam, vitae elementum sapien lobortis ut. Ut volutpat neque sagittis sollicitudin ultrices. Duis malesuada accumsan ante eu finibus. Ut efficitur libero eu laoreet efficitur. Cras non fringilla velit. Sed ut massa pellentesque diam facilisis auctor eu nec mi. Aenean ut venenatis nulla, vitae molestie ante. Phasellus maximus, velit et commodo semper, risus ipsum efficitur sem, sit amet egestas augue turpis ac velit. Proin sodales, mauris ut sollicitudin imperdiet, arcu erat posuere neque, eu laoreet sapien turpis mollis felis. Morbi sit amet metus nec massa suscipit eleifend. Donec nec congue magna. Nunc eros odio, viverra sed fringilla eu, commodo vitae quam.

            Proin condimentum orci gravida, semper libero at, dictum tortor. Aenean bibendum diam diam, sit amet elementum lacus interdum nec. Pellentesque id risus nibh. Quisque cursus scelerisque massa nec gravida. Nullam quis suscipit enim. Quisque bibendum leo ipsum, sit amet facilisis dolor lobortis ac. Proin et lacinia neque. Maecenas interdum justo et odio interdum, ut tincidunt urna cursus. Duis fringilla mattis orci, nec fringilla erat vehicula at.

            Nulla molestie orci sed sapien congue vulputate. Donec a ornare libero, eu porttitor sapien. Sed lacinia sagittis diam ac cursus. In efficitur nibh ac tortor posuere, et porttitor felis semper. Vivamus aliquet cursus tempor. Ut molestie mauris dictum lacus semper finibus. Morbi vitae est et sapien viverra volutpat eget ac lacus. Praesent iaculis scelerisque mattis. In pellentesque eget quam scelerisque viverra.

            Maecenas at molestie dui, ac lacinia arcu. Mauris mattis congue congue. Sed eu ligula velit. Cras purus nisl, euismod ultricies ultricies a, ullamcorper sit amet nunc. Aenean imperdiet velit at sapien malesuada egestas. Aliquam vitae facilisis purus, et ultricies elit. Duis vestibulum luctus accumsan. Fusce in ante vel est dapibus mollis.

            Praesent condimentum quam id porta ullamcorper. Mauris sit amet consequat leo. Pellentesque quis leo hendrerit, bibendum tellus in, tristique ante. Quisque ut ante vel mauris maximus laoreet. Cras eros nisi, pretium non scelerisque ut, porttitor in massa. Maecenas eleifend lobortis leo eu ultrices. Vivamus viverra rhoncus odio, ut suscipit mauris.

            Fusce velit purus, malesuada nec aliquet sit amet, commodo sit amet orci. Nullam facilisis, massa in maximus dignissim, ex quam fermentum sem, vitae ultricies ipsum sem ac nulla. Praesent vitae leo mattis, venenatis massa quis, pretium erat. Aliquam volutpat ligula eget scelerisque ultricies. Maecenas ullamcorper lacinia libero, et ultrices lorem. Mauris vitae pulvinar ligula. Donec at augue scelerisque, blandit leo eu, tincidunt ligula. Nunc blandit fringilla lacus ac commodo. Nulla sit amet libero bibendum, venenatis libero non, vestibulum elit. Aliquam non risus fermentum, vestibulum nisl vel, egestas orci. Morbi nunc augue, tincidunt at odio a, vulputate porta justo. Sed eleifend congue nisi in consequat. In nec quam rutrum, elementum tellus nec, sagittis elit. Vestibulum rhoncus mauris elit, vel tempor libero ullamcorper vitae. Integer imperdiet libero vestibulum magna dignissim dignissim.

            Nullam a suscipit orci. Suspendisse maximus, lorem sed posuere finibus, tortor elit bibendum justo, et ultrices urna justo at leo. Quisque at sapien odio. Nulla facilisi. Aliquam vel ipsum pretium, ornare risus eget, rhoncus risus. Integer lacinia eu quam a mattis. Integer cursus, mauris ac rhoncus tempor, lorem purus ultricies augue, id ullamcorper tortor nulla a purus. Sed vehicula tortor in fringilla finibus.

            In laoreet ipsum eget magna eleifend, at tempus tortor consequat. Donec ultrices volutpat auctor. Morbi faucibus odio ac libero ultricies, id vehicula eros ultricies. Sed fringilla erat in scelerisque euismod. Suspendisse semper libero et tellus lobortis faucibus. Aliquam feugiat sollicitudin tristique. Pellentesque consequat arcu at massa accumsan tincidunt. In varius augue vel ante mollis, ac venenatis metus viverra. Curabitur scelerisque vulputate eros, vel rhoncus tellus malesuada ac. Praesent in consectetur metus, sit amet interdum ante. In faucibus, augue eget sollicitudin convallis, elit magna faucibus elit, sed ullamcorper est dui aliquam mauris.

            Aenean nibh augue, consectetur nec rutrum vel, congue eget enim. Etiam ante massa, pharetra et eros id, elementum iaculis erat. Mauris purus elit, consectetur id mi et, congue gravida ex. Ut consectetur luctus elementum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Donec vitae nibh vestibulum ante ultricies bibendum nec non magna. Maecenas pharetra sapien posuere nunc imperdiet accumsan. Mauris sed metus massa. Curabitur ut posuere augue, eu fermentum est. Suspendisse orci ipsum, ornare eu pulvinar sed, mollis quis neque. Etiam rutrum lobortis ipsum, id pellentesque justo efficitur a. Phasellus id urna eget nisl blandit tempor ac in lorem. Sed dignissim lectus in blandit hendrerit.

            Praesent tempor leo sed urna malesuada, non blandit purus vehicula. Aenean ac odio odio. Curabitur tempus mollis finibus. Donec orci felis, tincidunt et lobortis vitae, facilisis in lacus. Etiam vestibulum magna eget est viverra fermentum. Nam vulputate dignissim nisi, a sagittis odio volutpat id. Praesent ut libero turpis. Vivamus ut facilisis libero, sed lacinia dolor.

            Etiam bibendum bibendum sem, ut bibendum leo. Praesent nec ante non odio viverra pulvinar. Pellentesque at ligula nec leo fermentum porttitor. Donec at blandit augue. Cras non tincidunt est. Sed congue ullamcorper justo id imperdiet. Mauris congue felis arcu, et faucibus sapien rhoncus in.

            Curabitur eget vestibulum ex. Phasellus aliquam dolor a ligula pellentesque, eget congue felis cursus. Nullam at eros pharetra, semper est ac, condimentum sem. Nunc ut dolor vitae dui scelerisque egestas. Nunc ut odio ut ligula ornare suscipit quis nec dui. Etiam ut viverra eros, in vehicula lorem. Fusce aliquam consectetur elementum.

            Aenean eu risus vel mi porttitor consectetur. Curabitur ac tempus nisl. Fusce vel nisl sem. Ut quis euismod elit. Nullam placerat, odio quis maximus condimentum, ipsum sapien dapibus ligula, nec ullamcorper lorem nisl et lorem. Aenean ultricies lorem ac turpis gravida ullamcorper. Vestibulum at massa porttitor, vehicula purus eget, eleifend urna. Quisque dictum auctor convallis. Aenean lacinia lacus in dapibus cursus. Morbi ultricies ornare lorem sed semper. Proin ultrices ex nec turpis ultricies vestibulum. Proin auctor cursus diam et auctor. Suspendisse ut purus bibendum, suscipit erat at, accumsan nisl.

            Nulla malesuada dui quis tellus congue, at egestas enim ultricies. Maecenas eleifend egestas condimentum. Sed bibendum pellentesque enim et dictum. Integer sit amet sodales neque. Ut lorem dui, auctor ac quam ut, consequat vestibulum velit. Duis neque lorem, feugiat vel arcu non, ullamcorper eleifend turpis. Etiam rhoncus sem in mattis mollis. Etiam fringilla tellus nec quam vulputate, nec consequat sem vulputate. Pellentesque sem lectus, venenatis non arcu sit amet, faucibus interdum risus. Nunc ut tincidunt ipsum. Integer tempor lectus eget ultrices rhoncus.

            Suspendisse venenatis, nisi quis lacinia iaculis, ante ante porta felis, at congue lectus dolor non justo. In malesuada feugiat malesuada. Curabitur lectus lacus, pharetra vitae ante a, ullamcorper consequat lorem. Donec vel sapien non augue maximus ultricies non ut lacus. Quisque eu varius diam, vitae gravida tortor. Vestibulum pharetra ex nec velit tincidunt sagittis. Pellentesque eleifend est in tempor facilisis. Mauris nulla mauris, aliquet in dolor nec, gravida interdum tortor. Vestibulum pretium tempus convallis. Nulla vitae fermentum est, id vestibulum erat. Nullam imperdiet lectus id cursus condimentum.

            Nulla in nulla imperdiet, dignissim quam eu, fermentum mi. Curabitur id hendrerit mauris. Donec a aliquam dui. Nulla facilisi. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Fusce consectetur pretium risus, eget facilisis libero varius ut. Phasellus mattis sapien sit amet mauris interdum fringilla. Suspendisse cursus lectus sollicitudin purus pellentesque mattis. Nam tellus nisi, molestie ac mauris a, imperdiet finibus mi. Curabitur nulla mauris, euismod vel finibus sodales, vehicula sit amet lectus. Suspendisse ultricies pellentesque porta. Duis et tellus ac turpis placerat bibendum. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum a lacinia nisl. Nunc magna sem, faucibus sed sollicitudin in, blandit lacinia ipsum. Integer vel faucibus justo.

            Nulla convallis felis urna, at viverra tortor fringilla ut. Fusce tristique pulvinar augue, sollicitudin sagittis diam vestibulum vitae. Nunc eget risus mi. Praesent a pretium metus, nec tincidunt turpis. Mauris a tellus at felis interdum rutrum in at ante. Pellentesque tincidunt egestas turpis, eget venenatis ante elementum et. In lobortis placerat nunc. Donec leo sem, feugiat et risus in, tincidunt ornare nisl. Cras eget tincidunt nibh, id varius augue. Sed sed orci sit amet urna ornare euismod at eu neque. Donec consequat dapibus aliquam. Duis efficitur neque risus, eu venenatis enim scelerisque vel. Donec facilisis id mauris vitae mattis. Vivamus varius mi metus, eget accumsan augue finibus sit amet.

            Nullam et interdum erat. In quis dui pulvinar, auctor turpis ut, dapibus erat. Morbi sagittis nibh vitae lacus porttitor, vehicula pharetra dolor semper. Ut convallis convallis sapien, et facilisis metus maximus in. Curabitur tempus quam a arcu ultrices faucibus. Vivamus dapibus, augue sed aliquam accumsan, erat magna facilisis justo, ut tincidunt urna nulla sed nisl. Nunc tempor porta ligula, quis ullamcorper nunc mollis in. Pellentesque dictum efficitur diam, at facilisis mauris.

            Pellentesque eget posuere arcu. Praesent finibus elementum libero sit amet elementum. Donec a mattis nibh. Donec eget scelerisque sem. Nunc ac enim id turpis finibus placerat. Vivamus semper dui ac neque feugiat molestie et a nisi. In cursus volutpat mi. Maecenas id bibendum magna. Cras id justo egestas, gravida augue convallis, finibus urna. Donec posuere ligula eget ex lobortis, sit amet pharetra lacus euismod. In facilisis sed eros vitae iaculis. Etiam non neque fringilla, scelerisque nibh eget, hendrerit lectus.

            Aenean vel ex magna. Aenean at tempor eros. Fusce facilisis lacus tempus mauris malesuada commodo. Nullam eleifend lectus sed justo commodo ultrices. Sed lacinia, ex vitae interdum consectetur, nisi diam faucibus justo, nec hendrerit ante leo volutpat sem. Fusce et odio vitae libero vehicula bibendum. In et lobortis orci. Maecenas vehicula lorem vel lacinia aliquam. Mauris ut libero in diam gravida varius.

            Donec eu erat non dui tempus placerat vel vel enim. Nulla maximus egestas mauris, ut lacinia libero. Cras a tristique tortor, non feugiat erat. Phasellus bibendum ullamcorper libero a laoreet. Phasellus laoreet nec metus in tincidunt. Etiam blandit id magna sed aliquam. Nulla sed aliquam lacus. Curabitur sit amet sem gravida, pharetra tortor quis, vestibulum sem. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut.
        """.trimIndent()
    }

    private fun getXML(): String {
        return """
            <CATALOG>
            <PLANT>
            <COMMON>Bloodroot</COMMON>
            <BOTANICAL>Sanguinaria canadensis</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}2.44</PRICE>
            <AVAILABILITY>031599</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Columbine</COMMON>
            <BOTANICAL>Aquilegia canadensis</BOTANICAL>
            <ZONE>3</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}9.37</PRICE>
            <AVAILABILITY>030699</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Marsh Marigold</COMMON>
            <BOTANICAL>Caltha palustris</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Mostly Sunny</LIGHT>
            <PRICE>${'$'}6.81</PRICE>
            <AVAILABILITY>051799</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Cowslip</COMMON>
            <BOTANICAL>Caltha palustris</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}9.90</PRICE>
            <AVAILABILITY>030699</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Dutchman's-Breeches</COMMON>
            <BOTANICAL>Dicentra cucullaria</BOTANICAL>
            <ZONE>3</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}6.44</PRICE>
            <AVAILABILITY>012099</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Ginger, Wild</COMMON>
            <BOTANICAL>Asarum canadense</BOTANICAL>
            <ZONE>3</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}9.03</PRICE>
            <AVAILABILITY>041899</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Hepatica</COMMON>
            <BOTANICAL>Hepatica americana</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}4.45</PRICE>
            <AVAILABILITY>012699</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Liverleaf</COMMON>
            <BOTANICAL>Hepatica americana</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}3.99</PRICE>
            <AVAILABILITY>010299</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Jack-In-The-Pulpit</COMMON>
            <BOTANICAL>Arisaema triphyllum</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}3.23</PRICE>
            <AVAILABILITY>020199</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Mayapple</COMMON>
            <BOTANICAL>Podophyllum peltatum</BOTANICAL>
            <ZONE>3</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}2.98</PRICE>
            <AVAILABILITY>060599</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Phlox, Woodland</COMMON>
            <BOTANICAL>Phlox divaricata</BOTANICAL>
            <ZONE>3</ZONE>
            <LIGHT>Sun or Shade</LIGHT>
            <PRICE>${'$'}2.80</PRICE>
            <AVAILABILITY>012299</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Phlox, Blue</COMMON>
            <BOTANICAL>Phlox divaricata</BOTANICAL>
            <ZONE>3</ZONE>
            <LIGHT>Sun or Shade</LIGHT>
            <PRICE>${'$'}5.59</PRICE>
            <AVAILABILITY>021699</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Spring-Beauty</COMMON>
            <BOTANICAL>Claytonia Virginica</BOTANICAL>
            <ZONE>7</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}6.59</PRICE>
            <AVAILABILITY>020199</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Trillium</COMMON>
            <BOTANICAL>Trillium grandiflorum</BOTANICAL>
            <ZONE>5</ZONE>
            <LIGHT>Sun or Shade</LIGHT>
            <PRICE>${'$'}3.90</PRICE>
            <AVAILABILITY>042999</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Wake Robin</COMMON>
            <BOTANICAL>Trillium grandiflorum</BOTANICAL>
            <ZONE>5</ZONE>
            <LIGHT>Sun or Shade</LIGHT>
            <PRICE>${'$'}3.20</PRICE>
            <AVAILABILITY>022199</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Violet, Dog-Tooth</COMMON>
            <BOTANICAL>Erythronium americanum</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}9.04</PRICE>
            <AVAILABILITY>020199</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Trout Lily</COMMON>
            <BOTANICAL>Erythronium americanum</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}6.94</PRICE>
            <AVAILABILITY>032499</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Adder's-Tongue</COMMON>
            <BOTANICAL>Erythronium americanum</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}9.58</PRICE>
            <AVAILABILITY>041399</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Anemone</COMMON>
            <BOTANICAL>Anemone blanda</BOTANICAL>
            <ZONE>6</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}8.86</PRICE>
            <AVAILABILITY>122698</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Grecian Windflower</COMMON>
            <BOTANICAL>Anemone blanda</BOTANICAL>
            <ZONE>6</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}9.16</PRICE>
            <AVAILABILITY>071099</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Bee Balm</COMMON>
            <BOTANICAL>Monarda didyma</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}4.59</PRICE>
            <AVAILABILITY>050399</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Bergamot</COMMON>
            <BOTANICAL>Monarda didyma</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}7.16</PRICE>
            <AVAILABILITY>042799</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Black-Eyed Susan</COMMON>
            <BOTANICAL>Rudbeckia hirta</BOTANICAL>
            <ZONE>Annual</ZONE>
            <LIGHT>Sunny</LIGHT>
            <PRICE>${'$'}9.80</PRICE>
            <AVAILABILITY>061899</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Buttercup</COMMON>
            <BOTANICAL>Ranunculus</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}2.57</PRICE>
            <AVAILABILITY>061099</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Crowfoot</COMMON>
            <BOTANICAL>Ranunculus</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}9.34</PRICE>
            <AVAILABILITY>040399</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Butterfly Weed</COMMON>
            <BOTANICAL>Asclepias tuberosa</BOTANICAL>
            <ZONE>Annual</ZONE>
            <LIGHT>Sunny</LIGHT>
            <PRICE>${'$'}2.78</PRICE>
            <AVAILABILITY>063099</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Cinquefoil</COMMON>
            <BOTANICAL>Potentilla</BOTANICAL>
            <ZONE>Annual</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}7.06</PRICE>
            <AVAILABILITY>052599</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Primrose</COMMON>
            <BOTANICAL>Oenothera</BOTANICAL>
            <ZONE>3 - 5</ZONE>
            <LIGHT>Sunny</LIGHT>
            <PRICE>${'$'}6.56</PRICE>
            <AVAILABILITY>013099</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Gentian</COMMON>
            <BOTANICAL>Gentiana</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Sun or Shade</LIGHT>
            <PRICE>${'$'}7.81</PRICE>
            <AVAILABILITY>051899</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Blue Gentian</COMMON>
            <BOTANICAL>Gentiana</BOTANICAL>
            <ZONE>4</ZONE>
            <LIGHT>Sun or Shade</LIGHT>
            <PRICE>${'$'}8.56</PRICE>
            <AVAILABILITY>050299</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Jacob's Ladder</COMMON>
            <BOTANICAL>Polemonium caeruleum</BOTANICAL>
            <ZONE>Annual</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}9.26</PRICE>
            <AVAILABILITY>022199</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Greek Valerian</COMMON>
            <BOTANICAL>Polemonium caeruleum</BOTANICAL>
            <ZONE>Annual</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}4.36</PRICE>
            <AVAILABILITY>071499</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>California Poppy</COMMON>
            <BOTANICAL>Eschscholzia californica</BOTANICAL>
            <ZONE>Annual</ZONE>
            <LIGHT>Sun</LIGHT>
            <PRICE>${'$'}7.89</PRICE>
            <AVAILABILITY>032799</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Shooting Star</COMMON>
            <BOTANICAL>Dodecatheon</BOTANICAL>
            <ZONE>Annual</ZONE>
            <LIGHT>Mostly Shady</LIGHT>
            <PRICE>${'$'}8.60</PRICE>
            <AVAILABILITY>051399</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Snakeroot</COMMON>
            <BOTANICAL>Cimicifuga</BOTANICAL>
            <ZONE>Annual</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}5.63</PRICE>
            <AVAILABILITY>071199</AVAILABILITY>
            </PLANT>
            <PLANT>
            <COMMON>Cardinal Flower</COMMON>
            <BOTANICAL>Lobelia cardinalis</BOTANICAL>
            <ZONE>2</ZONE>
            <LIGHT>Shade</LIGHT>
            <PRICE>${'$'}3.02</PRICE>
            <AVAILABILITY>022299</AVAILABILITY>
            </PLANT>
            </CATALOG>
        """.trimIndent()
    }
}
