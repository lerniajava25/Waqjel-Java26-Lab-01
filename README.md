# Waqjel-Java26-Lab-01
Här är en sammanställning av de referenser och resurser som applikationen bygger på, uppdelad i sammanhängande stycken för varje tekniskt moment:

## Internetanslutning och datahämtning (API)
För att hämta elpriserna i realtid ansluter applikationen till det öppna och kostnadsfria API:et som tillhandahålls av Elpriset just nu (https://elprisetjustnu.se). API-dokumentationen anger att data struktureras som en JSON-array för varje dygn och nås via en specifik URL-mall baserad på år, datum och elområde. Själva nätverkskopplingen etableras i Java med hjälp av standardklasserna **java.net.URL** och **java.net.URLConnection** från det officiella Java SE-biblioteket. Genom att konfigurera en User-Agent-header (som efterliknar en vanlig webbläsare) passerar nätverksanropet säkert genom lokala brandväggar och routrar utan att blockeras.

[Java URL Connection Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URLConnection.html)

## JSON-hantering och datamappning
När JSON-datan har tagits emot som en textström omvandlas den till strängkontext med hjälp av **java.nio.charset.StandardCharsets.UTF_8**. För att parsa (analysera) denna råa textsträng till typ-säkra Java-objekt används det externa open-source-biblioteket **FasterXML Jackson** Databind via klasserna **ObjectMapper** och **TypeReference**. Eftersom Java Records är immutabla använder Jackson intern reflektion i kombination med annoteringen **@JsonProperty** för att mappa JSON-nycklar som **SEK_per_kWh** direkt till fält i din Elpris-record.

[Jackson FasterXML Github Documentation](https://github.com/FasterXML/jackson-databind)

## Lokal lagring med fil-cache (File I/O)
För att spara den nedladdade informationen lokalt och minimera antalet nätverksanrop används paketet Java NIO (New I/O), mer specifikt klasserna **java.nio.file.Files** och **java.nio.file.Path**. Applikationen kontrollerar först om filen existerar på hårddisken med **Files.exists(Path)**. Om filen hittas läses den direkt in i primärminnet via **Files.readString(Path)**. Om den saknas laddas informationen ner från nätverket och skrivs omedelbart till projektets rotmapp med **Files.writeString(Path, String)**, vilket skapar en robust och offline-vänlig databashantering direkt på disk.

[Java NIO Files Utility Reference](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html)

## Prisberäkningar och algoritmer
De matematiska beräkningarna vilar på grundläggande datastrukturer och algoritmteori. Sorteringen av dygnets timmar görs genom att kopiera data till en **java.util.ArrayList** och applicera en anpassad **java.util.Comparator** som jämför flyttal kronologiskt. För att hitta de 4 billigaste sammanhängande timmarna används en optimerad **Sliding Window-algoritm**. Istället för tunga, nästlade loopar flyttas ett fönster med storleken 4 stegvis över dygnets 24 timmar, där det senaste timpriset adderas och det äldsta subtraheras. Detta ger en tidseffektiv beräkning med linjär tidskomplexitet **(\(O(n)\))** för att isolera det optimala laddningsfönstret.

[Sliding Window Technique](https://www.geeksforgeeks.org/dsa/window-sliding-technique/)