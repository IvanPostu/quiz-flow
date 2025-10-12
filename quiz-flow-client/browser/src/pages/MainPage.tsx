import React, { Fragment, useState } from "react";
import { Sidebar } from "src/components/Sidebar/Sidebar";

export const MainPage = () => {
  const [state, setState] = useState({
    sidebarIsShown: false,
  });

  return (
    <Fragment>
      <div className="wrapper">
        <Sidebar
          sidebarIsShown={state.sidebarIsShown}
          elements={[
            {
              headerText: "Pages",
              items: [
                {
                  text: "Profile",
                  icon: "lni lni-user",
                },
                {
                  text: "Task",
                  icon: "lni lni-agenda",
                },
                {
                  header: {
                    text: "Auth",
                    icon: "lni lni-protection",
                  },
                  items: ["Sign-In", "Sign-Up"],
                },
              ],
            },
            {
              headerText: "Tools & Components",
              items: [
                {
                  header: {
                    icon: "lni lni-layout",
                    text: "Form Elements",
                  },
                  items: ["Accordion", "Tabs"],
                },
                {
                  text: "Notifications",
                },
              ],
            },
          ]}
        />
        <div className="main">
          <nav className="dashboard-navbar">
            <button
              className="toggle-btn"
              type="button"
              onClick={() => {
                setState((prevState) => ({
                  ...prevState,
                  sidebarIsShown: !prevState.sidebarIsShown,
                }));
              }}
            >
              <i className="lni lni-grid-alt"></i>
            </button>
          </nav>
          <main className="content">
            Lorem ipsum, dolor sit amet consectetur adipisicing elit. Itaque
            suscipit autem numquam corrupti magni pariatur, quo quibusdam illum
            molestiae sit voluptas dolorum nihil explicabo iure unde impedit
            deleniti cum consequuntur accusamus. Illo repellat autem nisi, neque
            repudiandae accusantium voluptate, corrupti amet rerum commodi
            saepe! Dolorum, accusantium aliquid vitae suscipit exercitationem
            officia quibusdam ipsum modi nostrum dolor minus sunt aperiam dicta
            veritatis architecto necessitatibus, voluptates sint consectetur.
            Minus assumenda perspiciatis veniam fuga cum architecto consectetur
            culpa unde magni adipisci eum quisquam, totam eos et quibusdam, nam
            deleniti harum saepe veritatis ex quod maxime at repudiandae?
            Aliquam facilis est similique. Optio, laudantium! Lorem ipsum dolor
            sit amet, consectetur adipisicing elit. Laborum sunt tenetur
            doloremque sit esse ducimus in illum suscipit soluta rem officia a
            culpa ut tempore, enim, voluptate vero nemo quasi earum est dolorem
            deserunt facere error. Maxime cum veritatis voluptatibus quos quis,
            hic delectus ipsa deleniti perspiciatis cumque expedita dolor
            provident dolorem quod sunt ut eius quas temporibus porro sit
            voluptate? Similique commodi architecto omnis aliquam corrupti quam
            officiis ratione dolor natus perspiciatis, impedit molestias
            exercitationem quibusdam sapiente laudantium dignissimos vel tempore
            sed fugiat. At porro officiis optio fugit recusandae vero, laborum
            laboriosam eaque ad deserunt tenetur ipsam quidem, esse fugiat quo
            pariatur quae beatae! Facere, ad blanditiis necessitatibus
            repudiandae tempore, deserunt veritatis similique quia quasi
            consequatur, soluta placeat qui eligendi. Praesentium quis amet ab
            quasi, dolorem recusandae sapiente nam, iusto tenetur, rem commodi
            labore laborum cum molestias mollitia expedita? Deserunt, at
            repellat provident ex minus nulla consectetur! Sint officia
            temporibus, illum exercitationem enim eos velit atque obcaecati
            totam alias reiciendis doloribus possimus! Quo veniam laboriosam
            natus fugit. Atque iusto neque quibusdam voluptatem illo fugit
            delectus distinctio. Animi cumque odit enim quas aliquam repellat
            illum sit magni rem, commodi eum quos nostrum quasi reprehenderit
            hic sapiente consequatur illo quod iusto? Voluptas, fugiat dolores
            voluptatem laborum nesciunt delectus provident officia, quos,
            recusandae corrupti sit eius. Dignissimos, vero maxime inventore
            aperiam, impedit fugiat perspiciatis minima atque possimus suscipit
            obcaecati dolorem rem labore quia nulla totam explicabo sequi
            voluptates soluta perferendis eaque minus alias maiores! Id aliquid
            blanditiis officia harum. Voluptate dignissimos, maiores at, alias
            sequi soluta minus deserunt error, officiis fugit laboriosam
            eligendi voluptatem accusamus ipsa quos nobis officia earum harum
            non repellendus sunt modi quasi ducimus fuga! Cum molestiae,
            tempore, officiis itaque odio porro repudiandae reprehenderit hic id
            alias, aperiam nihil consequuntur corporis distinctio magni?
            Doloremque, nesciunt aut vel ipsam quo at corrupti unde laudantium,
            fuga iste quisquam eos. Nam magnam soluta consequuntur optio illo
            voluptatem omnis repellendus tempore assumenda illum. Itaque non
            atque, inventore architecto nesciunt cum eligendi optio nihil, iste
            consequatur dolorem pariatur voluptatem sint quas accusamus
            reiciendis fuga ea commodi, ut officia nostrum fugit id officiis.
            Exercitationem alias facilis dolores! Amet quam dolor eum et
            quibusdam facilis, magni aut rerum ut obcaecati placeat saepe. Eos
            libero, cupiditate atque, praesentium voluptas quisquam esse dolor
            dignissimos consequuntur corporis error aut possimus. Ipsa at hic et
            alias similique corporis animi impedit saepe sapiente deleniti,
            vitae est recusandae ipsam eius necessitatibus, facilis qui commodi
            adipisci? Accusamus soluta nam dignissimos placeat voluptatem sint,
            in blanditiis ex. Inventore, fugit consequatur dolore, laborum ullam
            sunt dignissimos asperiores blanditiis, aliquid ipsam dolores!
            Numquam, et unde dolorem in sunt eveniet facere necessitatibus
            voluptas dignissimos dolor, dolorum maxime adipisci doloremque
            voluptatem laudantium consequuntur? Optio ut placeat dignissimos
            sequi magni consequatur iusto a atque mollitia, debitis alias vel,
            neque voluptas ipsa similique numquam quae ab rem, veritatis quia
            at. Officia assumenda incidunt corrupti deserunt repudiandae odit et
            repellendus perferendis harum itaque provident, cumque, enim illo
            dolor est. Laboriosam aliquam, modi cum repellat deserunt
            dignissimos non natus dolores, nulla numquam vel, esse consequatur
            officiis at fugiat eius impedit recusandae voluptatum aliquid? Omnis
            perferendis culpa accusantium fuga tenetur reprehenderit eaque illo
            quod odio quisquam nulla iusto possimus unde repellendus
            exercitationem nostrum cumque facere et laboriosam quia fugiat,
            officiis vitae molestiae. Quo, maxime nobis! Doloremque vel quasi
            iusto maxime pariatur dolore voluptatem aut dolorum laborum, facere
            et cum temporibus. Veritatis ad impedit rem suscipit illum iusto
            adipisci repudiandae pariatur similique? Soluta hic inventore
            temporibus, dolor doloribus non quas eveniet pariatur dignissimos
            voluptatum necessitatibus quis nam voluptate deleniti, quasi
            exercitationem rem nesciunt ipsa tempora maxime commodi. Atque cum
            error non expedita totam et ad, voluptate repellendus rerum ipsam
            voluptatem accusantium eveniet! Quasi cumque at odit amet. Culpa
            ratione praesentium illo qui fugiat sequi enim optio architecto ex
            necessitatibus. Officia sint minima fugiat provident molestias alias
            necessitatibus natus non enim eligendi quam dicta dolores quisquam
            dolorum consequuntur temporibus, laboriosam recusandae, facere
            consectetur! Exercitationem, ipsam pariatur. Quibusdam adipisci hic
            accusantium, laboriosam blanditiis doloribus omnis accusamus saepe
            cumque optio minus tempore dolorem repellendus mollitia aperiam
            magnam maiores iste. Eligendi, quo! Iusto, sint sapiente dolores
            inventore nostrum mollitia tempore a quam nulla qui odio! Tempora
            excepturi mollitia doloremque at iusto necessitatibus, ducimus nobis
            laudantium ipsum commodi dicta aliquam maxime sit animi praesentium
            corrupti voluptatem quia error exercitationem ipsam. Ratione
            voluptatibus voluptates modi, similique sit quibusdam cum suscipit
            libero temporibus dolorem laudantium. Numquam tempore, eum dolorum
            nobis aliquam amet exercitationem? Hic illum error quod nostrum
            laborum cumque rem suscipit autem, voluptate odio delectus in modi
            fugit enim minus impedit. Deserunt ab soluta ad corrupti facilis
            officiis, consectetur dolorum? Libero a corrupti ullam nam sequi
            fugit ducimus perspiciatis, incidunt aspernatur aliquid vitae
            laudantium pariatur veritatis quibusdam quas earum esse autem labore
            sed qui ipsa aut tempore, aliquam illum! Sapiente quia itaque
            doloremque ipsa non assumenda nisi excepturi sit eaque! Mollitia aut
            soluta suscipit facilis minus vel et labore architecto itaque at ab
            consectetur temporibus fugiat officia veritatis, inventore
            cupiditate cum. Quo inventore assumenda reprehenderit culpa
            laboriosam modi quisquam, nostrum enim illum molestiae eum tenetur
            veniam maiores eaque, molestias velit? Veniam ab architecto dolore?
            Maxime laudantium iure a. Iusto sint, nemo quam quidem impedit
            consequatur reprehenderit enim pariatur quo eveniet saepe id ipsum?
            Reprehenderit asperiores illo obcaecati perspiciatis architecto
            autem at nihil ullam exercitationem, quod ipsum cupiditate
            necessitatibus tempore quidem odit labore sit. Rem cupiditate
            possimus error earum expedita culpa maxime saepe inventore. Ad odit
            fugiat assumenda praesentium atque aperiam facere velit, nisi
            voluptate exercitationem iusto, a reprehenderit quisquam, corrupti
            illum maiores non quidem possimus. Quo doloremque repellat ullam,
            sapiente iusto inventore blanditiis, quia maiores maxime, excepturi
            consequatur fugit. Voluptatum quidem impedit iusto. Nemo voluptate
            facere eius, distinctio rerum reprehenderit autem soluta iste labore
            nam eligendi recusandae molestias, at ducimus totam officia
            officiis. Iusto doloremque officiis nam voluptas distinctio aperiam
            architecto natus voluptatum! Ratione at minus, blanditiis
            perspiciatis doloremque totam explicabo voluptate sunt commodi animi
            doloribus enim eaque iste. Debitis dolorum ut aut quidem quaerat
            repellendus sint, explicabo rerum blanditiis natus quasi harum?
            Totam impedit, culpa illum architecto veniam quam unde fuga! Iste
            fugit temporibus aut libero recusandae illo inventore esse fuga enim
            tempora exercitationem, soluta adipisci. Maiores id nihil,
            repellendus pariatur sint repudiandae atque. Quidem hic autem vero
            laborum, natus, necessitatibus sunt minima praesentium ratione
            blanditiis perspiciatis ab corrupti obcaecati. Quaerat animi vel,
            tenetur necessitatibus quam odit saepe aperiam magni exercitationem
            mollitia minus corrupti soluta eaque tempore, similique vero sequi
            alias consectetur unde neque suscipit! Ab quo amet vero quia
            laudantium facere nam quidem fuga rem explicabo! Fugit mollitia odit
            consequatur excepturi? Molestias corporis commodi dicta laboriosam
            vero doloremque earum, aspernatur harum! Hic quas maxime consequatur
            doloribus vel ipsam. Deleniti eveniet autem saepe quam qui aut
            eligendi. Beatae eum odio soluta natus consectetur nam vel quisquam
            provident fuga rem, dicta aut aliquam doloribus delectus velit
            aspernatur inventore magnam repellendus, omnis eveniet quos dolorum
            reprehenderit voluptates consequuntur? Voluptate eligendi unde
            minima temporibus autem hic doloremque laudantium facilis quaerat
            animi aspernatur maxime soluta aut architecto tempore esse aliquid,
            doloribus ut. Expedita doloremque temporibus consectetur recusandae
            placeat, velit dolor aut ducimus assumenda ratione voluptatum,
            suscipit provident, a neque nesciunt sunt? Quasi, beatae. Blanditiis
            sequi possimus quo qui sapiente nulla eius veniam quas ullam dolorem
            quod vero modi ab laudantium magni, quaerat corporis itaque, dicta
            doloremque fugiat rem quidem! Deleniti illum veritatis, iure placeat
            quasi, quod animi architecto maiores tempore praesentium voluptatem
            reiciendis ullam ratione molestiae id, similique adipisci. Ullam
            magni quis eum? Quos reiciendis illo quae iure necessitatibus ullam
            tenetur, porro id labore iste. Porro, quo, quis autem, cumque
            veritatis incidunt libero saepe ad impedit hic laudantium qui
            blanditiis consequuntur sequi. Accusamus, tempora! A quidem atque
            optio iste consequatur quisquam! Animi ab facilis quaerat non dicta
            sit nulla hic cum, voluptatum asperiores? Placeat doloremque
            officiis suscipit velit qui laudantium adipisci numquam accusamus
            ipsam, a praesentium recusandae fugit, illo perferendis voluptate
            unde autem laborum et nihil, odit doloribus. Inventore debitis nihil
            cupiditate quaerat eos minima beatae ullam! Animi dicta laborum rem
            aliquam odit quisquam distinctio velit id atque. Totam eveniet
            deleniti iste ex, et eius officiis quibusdam ea? Iusto delectus
            rerum explicabo cumque temporibus sed facilis ipsa, ut aut
            voluptatum error saepe quia quae natus quasi odit exercitationem
            autem impedit, iure laborum, similique ad dolore quam quod. Veniam,
            ducimus ratione, sequi, cumque laudantium placeat quibusdam est eius
            incidunt ex tempora soluta id qui magnam ea voluptas. Error cumque
            voluptates modi veritatis tenetur fuga dolore molestias iste
            assumenda perferendis. Quidem pariatur cupiditate blanditiis quam
            eveniet. Quaerat inventore, minima quam culpa magnam, perspiciatis
            maxime earum minus nihil atque eveniet quo deserunt. Veritatis
            repellat dolore saepe ad quam officia sequi dolores magni quasi
            repudiandae. Cumque quisquam quas, dolore soluta exercitationem
            explicabo temporibus quaerat repellat totam iusto voluptates
            voluptate labore, obcaecati nam, assumenda modi magni? Possimus
            necessitatibus, quod iste quam itaque suscipit blanditiis dolore
            fugit voluptatibus, a deserunt ut molestias illum ex error incidunt
            ipsa deleniti unde porro non! Voluptas maiores blanditiis
            repellendus, reprehenderit dolorem quo fuga totam minus architecto
            quibusdam natus aliquid eius cumque officia adipisci, itaque animi
            provident corporis veritatis perspiciatis. Illo rerum, dolorem
            excepturi reiciendis dolores natus perferendis numquam recusandae
            nisi a aut ratione delectus vero itaque totam molestiae, quasi
            minima ipsum pariatur temporibus nostrum ipsa tenetur! Nostrum
            provident error dignissimos expedita consectetur accusantium
            laudantium sapiente tempora eius atque, temporibus quis minus
            aperiam cupiditate dolore commodi. Labore, officiis molestias
            exercitationem accusantium quis odit beatae nulla similique nemo
            tenetur illo voluptatem explicabo atque nobis fuga minima sapiente
            perspiciatis ad est accusamus? Excepturi magnam quidem porro
            similique quia harum? Ea in veniam eligendi, labore et itaque alias!
            Quas, consequatur, aut, magni eos iusto voluptatum accusantium
            eligendi quia non aliquid quisquam repellendus dolore temporibus
            possimus nihil. In sed similique optio deserunt temporibus molestias
            quia accusantium, explicabo ducimus ipsum soluta? Animi sapiente
            eveniet nulla a porro nisi odio repellat nobis, rem, error veritatis
            corrupti veniam unde dicta ipsum possimus aspernatur atque harum
            minus assumenda. Placeat molestiae labore voluptatibus similique
            rerum vel nostrum adipisci eaque quas sit magnam cum repudiandae
            cumque laborum facilis blanditiis possimus minus facere soluta est
            aut quia, ipsum perspiciatis. Laudantium dolor quod enim sapiente
            culpa nihil maxime non nostrum ea, harum tenetur ratione distinctio
            explicabo nobis commodi aliquam, nesciunt fugit. Et provident
            quisquam quam quos similique repellat. Nobis quis rem consequatur.
            Facere beatae tempora nostrum officiis odit! Non est impedit nam
            esse natus quo repellendus quis? Qui fugit possimus totam unde
            temporibus suscipit optio iure assumenda dicta, cupiditate rem
            molestiae a laborum dolore quis provident ab molestias doloremque
            eveniet cum enim! Laboriosam consectetur cum recusandae dolores
            veniam doloremque quasi beatae, distinctio inventore deleniti amet
            fugit aliquid expedita error enim commodi! Vero, sit nisi. Suscipit,
            vitae itaque, vel molestiae delectus maxime quod fugiat numquam
            dicta repellendus, explicabo iusto totam sequi ullam quam. Assumenda
            dolores molestias, blanditiis rem qui aperiam neque maiores natus
            itaque doloremque ipsa maxime beatae consequatur harum consequuntur
            veritatis enim porro velit, architecto laborum vel, unde illo?
            Temporibus id esse atque sed non rem, corrupti tempore animi
            voluptatum exercitationem quae autem vero deleniti laboriosam
            repudiandae totam, iusto, voluptas maxime aspernatur? Illum, optio
            hic eos exercitationem ullam nesciunt id pariatur perspiciatis
            asperiores qui saepe ad omnis voluptate alias ea at vitae non rem
            voluptatibus fugit deleniti accusantium impedit ab! Similique
            consequuntur ipsa consectetur consequatur, ullam quod ducimus
            inventore vero, incidunt pariatur dolorem corporis expedita eum ad
            accusamus, maxime nam provident minima recusandae quibusdam tempora!
            Laborum, eveniet facilis voluptatem sint iure et illum maxime sequi
            nostrum laboriosam deserunt provident architecto consequuntur
            inventore molestias? Ipsa reiciendis modi labore a voluptates, natus
            ratione tenetur commodi iusto quas expedita accusamus itaque,
            accusantium placeat officia laboriosam exercitationem consequatur
            beatae culpa doloremque, laborum ab! Consectetur magnam id saepe
            sequi corporis eum excepturi omnis et exercitationem repudiandae, a
            sit tenetur vel beatae quas nisi nemo! Quisquam vero atque
            consequatur. Repudiandae maiores, obcaecati optio minus deserunt non
            voluptatum reiciendis officia odio, autem perferendis sed dolorum
            aspernatur vel, ad dicta quo cumque dolor pariatur. Quis ea porro
            voluptatem? Mollitia deserunt soluta, id voluptates, consequuntur
            aspernatur, omnis libero tempore eveniet sequi itaque vel quasi
            ducimus hic perferendis corrupti eius aut! Quo corrupti quasi amet
            perferendis, quaerat, a et neque quam, aut officia doloribus dolor
            ipsam reprehenderit. Cum nam eos fugit debitis aperiam optio
            veritatis ipsum quae eligendi ratione voluptatum fuga ipsa error
            consectetur dignissimos, voluptates consequatur. Nobis, magnam
            quasi. Tenetur impedit veniam debitis omnis repellendus doloremque
            recusandae. Quam maxime totam eveniet obcaecati! Magnam esse
            cupiditate labore itaque! Sunt, nulla unde dolore accusamus eum iste
            nisi aliquam quibusdam molestias, magni fugit. Veniam cum assumenda
            sit libero ipsum pariatur enim illum rerum eaque tempora culpa
            laboriosam dolorem quisquam odit delectus id, accusantium voluptatem
            in corporis inventore blanditiis nemo. Expedita, cum sequi. Ratione
            ad quasi delectus deserunt eveniet quae fugit. Minima eum impedit
            sequi quos delectus, omnis aperiam qui nulla ad repellendus dolorum
            laudantium culpa id sint quisquam totam numquam ipsa. Odit fugiat
            ipsam vel aliquam esse, minima, omnis minus voluptatibus laborum aut
            fuga, illo exercitationem enim? Sint reprehenderit inventore ad
            quibusdam, accusantium cumque, exercitationem repellendus non nobis
            pariatur dolore ut quia fuga voluptates eaque facere id hic, ullam
            unde odit autem consequatur expedita natus. Tempora vel quod ullam
            nesciunt nihil nisi sint magni alias rem. Molestias, delectus? Sit
            voluptate doloribus ex recusandae dolor dolore, iste architecto
            nostrum soluta vero eveniet fugit aliquid velit repellat, explicabo
            ea ad ducimus nihil libero culpa! A laudantium quia dolore dolorum
            dignissimos magnam? Veniam tempora exercitationem impedit nulla.
            Modi neque repellendus doloribus itaque impedit maiores ratione in
            tempora similique corrupti cupiditate consectetur eum optio corporis
            incidunt veritatis autem, architecto tempore aperiam id illum iste,
            dolorem voluptatum? Eveniet consequatur laborum minus. Beatae
            asperiores, iusto voluptates doloremque deserunt sint, ipsam
            necessitatibus quasi illo inventore cumque quisquam laborum eligendi
            autem possimus impedit placeat! Earum consequuntur culpa,
            perferendis eligendi molestias dolore in illum vel dolorem qui
            mollitia possimus eos asperiores rerum inventore blanditiis incidunt
            facilis accusamus expedita quis odio, illo, ut fuga cumque!
            Excepturi laboriosam in nostrum accusamus quos doloremque aspernatur
            sed, id ullam, necessitatibus eaque! Inventore libero distinctio
            sapiente nulla ea doloribus error, deserunt nam obcaecati quidem
            blanditiis natus a consequuntur maiores ad, ullam explicabo nisi
            dolorum voluptas laudantium nesciunt magnam eaque magni soluta!
            Voluptate optio minima accusantium quisquam laborum perspiciatis,
            vel eligendi inventore molestias magnam? Mollitia ipsa odit
            voluptatibus. Non hic ipsa, facere quae fugiat quos adipisci sit ut
            ducimus. Voluptatum, facere quas. Deleniti molestiae recusandae odio
            dolorum dignissimos, enim officiis tempora unde doloremque,
            molestias placeat, porro totam aperiam harum magni aliquam esse
            necessitatibus illo veniam ipsa. Fugit unde numquam minus nam
            inventore dolor animi laudantium rerum, dignissimos deserunt
            consequatur pariatur quia temporibus dolore nulla similique
            asperiores praesentium blanditiis eligendi ad nemo ex eius quis!
            Possimus aperiam, magni ut iusto, consectetur maxime fuga dicta enim
            velit et earum suscipit, illum sit eos quis distinctio in similique.
            Atque dolorem voluptatum perspiciatis excepturi. Amet ea corporis
            quia qui! Pariatur molestias ea nulla harum aliquid consequuntur
            neque quidem beatae dolores voluptatem tenetur aut rerum ex unde,
            optio temporibus rem earum amet repudiandae! Repudiandae deleniti,
            dolorem corrupti similique maiores officiis aliquid non consectetur
            eaque? Aspernatur voluptatem, cumque qui ab reprehenderit veniam cum
            a officia maxime dolor alias pariatur saepe laboriosam itaque quam
            delectus asperiores maiores quia. Iure, ducimus voluptatibus! Rerum
            distinctio recusandae vero aliquid expedita sequi animi quis
            temporibus veritatis quas beatae, fugiat nostrum itaque dolorem
            facilis ex eum quaerat aliquam? Incidunt dignissimos, nisi ea nemo,
            consectetur omnis mollitia sequi delectus eos fuga repellendus
            pariatur repudiandae consequuntur quia magnam culpa in nostrum. Quos
            eius deleniti modi quis enim cum beatae eveniet, voluptatem
            molestiae adipisci nulla sapiente facere quia aliquam, aliquid,
            aperiam ab rem. Sit magni fuga voluptatum corrupti adipisci nesciunt
            in aperiam odio similique ipsa nostrum, illo hic laboriosam quos
            aliquid cupiditate ea soluta magnam voluptatem expedita eum.
            Dolorum, libero! Dolores nihil at aspernatur optio labore iste
            deleniti laboriosam ad odit. Quae libero perferendis neque natus
            odit rem, modi mollitia fuga quisquam delectus voluptatum voluptatem
            ad cumque dicta, ullam possimus a. Quasi, expedita explicabo.
            Incidunt doloremque officiis, quas amet sapiente natus eius at
            laborum error eligendi possimus magnam delectus non soluta harum
            neque recusandae dolore dignissimos! Quos consectetur cum accusamus
            reiciendis repudiandae autem sequi ut pariatur cumque magnam
            dolorum, aliquam temporibus minima nihil veritatis? Inventore ipsa
            doloribus fugit tempore aperiam officia sapiente architecto iure
            reiciendis eum odit nisi, voluptate beatae iusto magnam sint eos,
            dolor excepturi? Quisquam rerum necessitatibus, ducimus dolores
            minima incidunt ipsam dicta sit! Non hic dignissimos dicta veritatis
            quaerat aperiam recusandae, quia ullam adipisci vitae earum
            repudiandae repellendus molestias iste qui ipsam corrupti accusamus
            rerum? Temporibus magni corporis quo culpa assumenda minima
            voluptates veniam laborum deleniti debitis sequi quas distinctio
            ipsa repellat perferendis labore ipsam, hic eius libero
            consequuntur, aliquam doloribus a? Iusto est tempora cum aspernatur
            et. Consequuntur eius recusandae deserunt velit iure laborum, vitae
            veniam repellat libero molestiae et voluptatem natus reiciendis
            nulla debitis vel qui delectus itaque quae optio nihil aut amet ea
            similique? Iste tempore consectetur praesentium doloribus
            temporibus. Incidunt et dignissimos quod, qui maxime deserunt magni?
            Exercitationem itaque in incidunt inventore totam officia obcaecati
            quod laborum, numquam consequuntur aliquid animi nesciunt labore
            libero temporibus consequatur assumenda? Necessitatibus inventore,
            cumque error accusamus recusandae distinctio, dicta repudiandae
            dolore nisi adipisci aperiam et odit sunt. Sequi, doloribus
            voluptate illo porro iure fugit reprehenderit pariatur dolorum
            harum, laboriosam molestias sed earum ullam est, error quisquam ab.
            Obcaecati autem, tempora animi amet aspernatur harum nam ullam error
            delectus asperiores alias fuga officia, quos possimus! Molestias,
            facilis reprehenderit distinctio nihil accusantium optio. Placeat
            aperiam esse suscipit quisquam dolorem similique, excepturi quasi
            sapiente recusandae dolore voluptatibus sequi ad, non ratione?
            Ducimus, explicabo suscipit temporibus quod molestiae soluta earum
            sint, placeat quam laboriosam possimus libero non asperiores,
            assumenda ab dolore eligendi et quis beatae modi aut expedita.
            Perferendis reiciendis accusamus quia odit quisquam, eligendi
            molestias laborum assumenda aperiam totam placeat voluptatibus aut
            explicabo cupiditate at velit voluptas ipsam sequi pariatur
            laboriosam necessitatibus vitae maiores iste? Accusantium sed
            blanditiis veniam quibusdam cupiditate ab sint dignissimos tempore
            inventore at, asperiores quisquam molestias eligendi libero incidunt
            ipsam quos architecto soluta odit laborum recusandae optio? Eos nam
            dolores ipsum odit reprehenderit unde natus saepe asperiores
            aspernatur ipsam aliquid fuga tempore doloremque cum quibusdam
            beatae molestiae ratione, nobis sunt. Unde neque voluptate ullam,
            vel vero magni praesentium deleniti incidunt quod tenetur, odio,
            quasi natus! Vel eligendi quaerat dignissimos consequuntur at
            deserunt, itaque ipsam quibusdam, adipisci ex aspernatur nam nostrum
            facere! Soluta nam illum facere reprehenderit ipsam. Rerum provident
            nihil repellat ab voluptatibus, accusantium sit incidunt blanditiis
            praesentium? Facere, modi ullam in rem tempore, deserunt, fugiat
            ratione dolore maiores voluptates assumenda molestias. Vero, vel
            reprehenderit. Maiores, sed. Natus rerum placeat quaerat dolores
            aperiam voluptatibus, nulla, non aut doloremque quo fugit pariatur
            molestiae earum ea aliquid repellendus accusantium facilis beatae
            ipsam asperiores totam, consequatur distinctio saepe! Perspiciatis
            ipsa aut assumenda blanditiis, voluptas magnam quaerat ad
            consequatur quam? Quibusdam consectetur vero fuga repellat, veniam
            aperiam nesciunt illum quia magnam blanditiis iure quo repudiandae
            obcaecati dolorem. Amet laboriosam ipsa quasi! Hic obcaecati aliquid
            quaerat qui tempora, fugit optio modi praesentium aperiam harum est
            dolores veritatis illum repellendus possimus minus, quisquam illo
            saepe eos at. Rem, ut veniam. Eaque fuga voluptatem illum explicabo.
            Nemo dolore, labore possimus vel illo dolores aliquid neque libero
            saepe similique reprehenderit perspiciatis, id veritatis ullam,
            fugiat quisquam impedit maiores ut non. Ducimus blanditiis maxime
            reiciendis praesentium magni nesciunt alias culpa! Exercitationem,
            amet dolores? Numquam vero, commodi facere natus, necessitatibus
            autem eius aliquid laboriosam labore esse delectus quos at corporis
            voluptatibus obcaecati dolor sed possimus perferendis provident
            veniam. Itaque veniam unde illo quam similique saepe voluptatum
            atque! Non totam ut expedita voluptatem placeat quibusdam nihil
            animi quam, ducimus hic ipsa, modi beatae iure, maxime quae
            cupiditate possimus aspernatur reprehenderit vitae iusto incidunt?
            Tenetur, veniam exercitationem? Cupiditate sed nihil rerum
            praesentium optio! Adipisci beatae ab magni at culpa illo
            perspiciatis, omnis, molestiae architecto accusantium quis sint
            veniam ducimus. Perferendis nulla esse reiciendis adipisci eveniet,
            fugiat tempora vero illo ducimus magnam, veniam error. Commodi
            repudiandae quaerat ad eveniet quae, minus quos explicabo ab
            doloremque cupiditate iure sunt quidem, incidunt facere optio
            expedita facilis sint autem! Facilis, placeat cumque atque dolorem
            voluptates eaque nihil et ullam fugit laudantium quasi voluptas nam
            repellendus at, nostrum magni sed dolore animi perferendis dolorum
            maxime! Iste consectetur distinctio quos, ducimus delectus quis nemo
            consequuntur. Quibusdam deserunt maiores repellat odit sapiente
            suscipit et quasi temporibus ea, fugit ad. Eveniet quibusdam a porro
            explicabo dolores sequi voluptas quo libero veritatis quis dolore
            commodi maiores ducimus modi neque perspiciatis accusamus, rem alias
            impedit amet vitae? Sequi exercitationem quam, cumque placeat
            voluptatum modi et esse suscipit ducimus illum nisi eligendi unde
            tempore ipsa? Perspiciatis officia eveniet ea commodi animi
            adipisci? Ratione, delectus, explicabo deleniti ullam asperiores
            enim, libero dicta atque similique at esse earum exercitationem a
            necessitatibus quo expedita minus minima sed sint! Repellat quae id
            autem quibusdam ipsum minus dolores voluptatem esse error quidem
            porro nulla cumque in quia consequuntur, alias doloribus aliquam
            consequatur odio magnam incidunt. Veniam vitae et sint laboriosam
            repudiandae unde excepturi magnam alias atque eveniet veritatis,
            commodi nam earum accusamus eius in sit rerum! Similique ducimus
            corporis aspernatur consequatur voluptatum dolor, pariatur error
            eligendi exercitationem ipsam consectetur vitae quos repellendus,
            doloribus quo saepe, repudiandae quidem aliquam fugiat. Eaque neque
            rerum vero natus, eius animi consequatur incidunt assumenda non at?
            Laudantium tenetur laboriosam id dolor nihil reprehenderit velit
            officiis, iusto rem provident quis repellat voluptatibus quisquam
            modi officia et libero ab eaque, optio possimus. Quam aliquam magnam
            neque quos pariatur necessitatibus earum, modi fuga ducimus corporis
            explicabo, quis quod, provident dolorem sed vitae est corrupti
            officiis inventore delectus. Ratione perferendis ducimus voluptates
            rem praesentium illum sint, cupiditate, totam, facilis esse repellat
            earum iusto molestias beatae! Repellendus repudiandae facere nihil?
            Vero sunt quos incidunt, deserunt distinctio ducimus, maiores
            ratione voluptate fuga molestias velit officia reiciendis blanditiis
            iure nesciunt? Quibusdam velit repudiandae harum possimus sed
            aspernatur quos consequuntur provident, adipisci voluptate aut
            ratione iure, nulla aperiam deleniti commodi dolorem repellendus
            quasi saepe. Voluptatem ipsum placeat ipsa soluta expedita quidem.
            Iusto optio excepturi possimus expedita animi cum hic vel! Iure quis
            laudantium ipsam sunt voluptatem ea exercitationem? Sed veniam
            excepturi rem nihil repellat, culpa blanditiis quia provident! A
            reprehenderit unde labore, eos deleniti aliquid? Corrupti
            recusandae, cum ipsam sapiente possimus earum totam laborum aliquam,
            eaque doloremque consequuntur illo omnis. Eos architecto laborum,
            minima cupiditate alias placeat tempore esse tenetur rerum porro,
            vero laudantium corrupti sequi recusandae maxime? Aliquam quod
            doloremque eum et officia accusamus quae iste distinctio quidem
            facilis beatae fugiat non odio a eaque autem ullam rerum sapiente
            debitis corrupti nesciunt, exercitationem ipsa at. Deleniti
            laudantium modi mollitia, voluptate, nisi animi vitae natus neque
            quam, rerum corrupti. Nihil blanditiis non totam. Autem earum, esse
            suscipit enim nihil, excepturi accusantium atque porro tenetur
            laudantium quos optio similique eligendi id? Ipsam dolore officia
            aliquid, nisi esse sit pariatur repudiandae? Iste atque, eos velit
            error nisi suscipit, optio facere inventore quidem aperiam
            dignissimos quaerat sapiente, voluptas quo sunt deserunt nostrum
            veritatis veniam! Commodi modi odio placeat ullam quo repellat quos,
            quia molestias eveniet, quaerat, sapiente nobis. Alias voluptatibus
            eum error libero impedit doloremque illum recusandae. Molestiae sunt
            rem tempora? Deleniti dolore sint exercitationem nulla officiis
            repellat facilis sed quisquam minima aut saepe laborum, consectetur,
            maiores id, quasi enim. Minus perferendis eaque ut provident
            deleniti? Nulla, nostrum ipsa? Aut odio facere itaque, ad quod
            nesciunt laboriosam nemo dignissimos! Dolorem totam nemo
            consequuntur ullam minima ipsam qui doloremque, earum, nostrum et,
            deserunt ipsa magni labore provident vel. Asperiores eveniet
            perspiciatis iusto dolorum adipisci. Ipsam doloremque libero sint id
            iste blanditiis provident explicabo est sit corporis vitae
            temporibus minima, mollitia, quibusdam nostrum aut deserunt veniam
            non quam quod excepturi vel adipisci ratione omnis! Accusamus autem
            quidem, dicta suscipit ea consectetur iste id error ducimus
            cupiditate nobis ex corporis quo aperiam quasi vero ullam
            repudiandae. Cumque rerum nemo eius deleniti in accusamus commodi
            totam praesentium consequatur necessitatibus ab non odio provident
            impedit eaque iste, similique sed dolore illo beatae voluptatem
            maiores omnis consectetur blanditiis! Exercitationem, illum! Nisi et
            voluptates mollitia, sapiente aperiam possimus commodi eos, iusto
            voluptas enim ducimus tenetur suscipit aliquam. Officia velit,
            quidem nemo itaque reprehenderit corrupti fugit quam modi
            repellendus sint atque cumque voluptatem totam ducimus. Repellat
            autem aut excepturi adipisci quaerat vel sit eaque labore provident
            dolores quod aliquid laborum officiis a libero, veritatis nesciunt
            inventore quae optio sequi fugit iusto quas vitae sed! At suscipit
            et dolores vero! In provident aperiam hic dicta, illo voluptates
            iure quam repellendus deleniti amet! Facilis tempora obcaecati nulla
            inventore incidunt dolorem, perferendis eos saepe sed distinctio
            eius libero dicta unde veniam delectus accusamus amet tempore maxime
            recusandae ad repudiandae ea iusto pariatur? Dolore, magni
            consectetur laboriosam quibusdam eveniet, maiores voluptas suscipit
            eaque ipsam omnis consequuntur minima hic quo modi, minus sunt! Est
            reiciendis vel ipsum hic dolor fugiat in nihil obcaecati nisi. Amet
            ab maiores commodi quaerat possimus explicabo tenetur quas, fugiat,
            omnis voluptatibus excepturi laborum, impedit soluta corporis quidem
            et. Debitis, alias voluptates iste et consectetur inventore mollitia
            asperiores omnis rem eos odio sit, autem dolorum distinctio
            reprehenderit? Est ratione quae voluptates quasi reiciendis. Magnam,
            aut optio. Saepe, commodi pariatur ea dolorum dicta impedit
            excepturi sint voluptatem, corrupti totam minima error blanditiis,
            laborum beatae officia. Soluta quam a incidunt animi. Obcaecati non
            accusantium illo sequi vitae ad, reiciendis quas voluptas
            consequuntur cumque sunt necessitatibus voluptatum? Ipsum, esse
            molestiae repellendus accusantium tempore obcaecati, nam sit ab,
            dolorem libero dolores a fugit soluta maiores in dicta voluptate
            reiciendis enim. Voluptatibus corporis quidem maxime, reiciendis
            sequi inventore quisquam libero debitis qui, natus eum at, enim
            facere tenetur. Aperiam dignissimos maiores, dolores ullam officia
            modi aliquid. Molestiae, itaque! Odio, non fuga dolores voluptates
            assumenda commodi! Quos commodi perferendis voluptate ex quas
            obcaecati fugiat eaque ipsam fugit asperiores, ipsa sint voluptatum
            esse natus corrupti sapiente voluptas ea excepturi, et alias
            ducimus. Quaerat accusantium aliquid natus, fugit at minima enim
            iste, repudiandae impedit, voluptates voluptatem quo exercitationem.
            Iste excepturi, voluptates sed veritatis ipsa animi exercitationem
            suscipit distinctio voluptatem dolorum ipsam et commodi corrupti
            incidunt quos eligendi harum beatae quisquam, minus explicabo magni
            hic! Quaerat, unde a eligendi iure nihil nesciunt dolores doloremque
            possimus, obcaecati accusamus minus sed! Culpa doloremque sed
            placeat itaque enim nam rem similique consectetur veritatis cum
            reiciendis cumque quae, nihil velit error deleniti rerum soluta
            nostrum quis optio reprehenderit ut harum, illum repellat! Quos
            ducimus dicta, molestias sequi sint omnis aliquid veniam, quod in at
            inventore. Dolor commodi voluptas, quae ut ipsam distinctio quisquam
            fugiat. A recusandae magni omnis vitae nesciunt ipsum placeat dolore
            laborum, nobis esse nihil culpa delectus dicta assumenda doloremque
            repellendus minus temporibus sit molestias corporis accusamus nulla
            laudantium voluptates magnam. Beatae libero harum labore quae sequi.
            Sunt odit repudiandae consequatur totam mollitia accusamus!
            Praesentium officiis aut nostrum debitis nulla itaque autem ipsum
            sit eius! Dolor alias deleniti eligendi dolorum aliquid soluta
            consectetur officiis dolores! Ea repellat consectetur deleniti
            sapiente saepe cum error quisquam soluta molestiae odit, temporibus
            ullam non impedit fugit perferendis nostrum assumenda sint doloribus
            vero. Fugit, dolorem voluptatem rerum voluptate atque excepturi
            perferendis quas ea beatae est iste vitae ipsa, et laudantium, ipsam
            nihil quod nemo quam dicta! Sint nihil officia pariatur est. Ipsum
            amet sunt sapiente, perspiciatis reiciendis eius temporibus
            voluptatum? Itaque quaerat molestiae omnis impedit error ab saepe et
            architecto nisi sequi eos unde corporis obcaecati animi debitis
            iusto autem consequuntur nihil labore quae, eum quia? Perspiciatis
            quo, nihil assumenda non explicabo doloremque illum deserunt ea
            corrupti dignissimos quod fugiat expedita sint, earum quidem minus
            pariatur eius consequatur natus, distinctio obcaecati esse
            consequuntur! Error pariatur ad ducimus fugiat inventore fuga,
            dolorem cum amet! Ut neque minima rem qui eligendi nemo inventore!
            Omnis enim ut veritatis eum nam, optio nesciunt porro sed non earum
            blanditiis, corporis neque fugit accusamus laborum architecto
            ducimus praesentium molestiae. Officiis, quae? Nobis non temporibus
            soluta voluptas, quas voluptatum repellat vitae sequi error quos
            assumenda, quo eligendi nemo consequuntur cum sed accusamus
            doloremque dicta minus expedita. Cumque beatae vitae consequuntur
            animi nesciunt aut blanditiis provident, tempora saepe numquam
            soluta fugit expedita accusantium eligendi neque doloremque error,
            consequatur officia quas placeat voluptas eos aliquam aliquid
            perspiciatis. Veniam dolores ullam repellendus iure incidunt, dicta
            rem corrupti perferendis ipsum maxime excepturi animi soluta maiores
            doloremque iusto blanditiis optio eaque, iste, sed nam delectus
            repellat. Impedit ex laudantium unde inventore neque architecto
            alias deserunt iusto, vero et facilis nulla. Quam iusto, placeat at
            ipsa est nihil facilis doloremque impedit, odit sint accusantium
            optio quas, sequi expedita? Ipsa nobis consequuntur qui tempora
            harum et exercitationem laborum odio omnis, deleniti alias
            necessitatibus maxime tempore hic, porro fugit sapiente impedit eos,
            enim neque. Error fugiat labore voluptatibus earum? Totam
            necessitatibus quia eaque, nihil itaque nulla! Ratione, quam
            commodi. Laudantium, ullam natus quisquam aliquam quia expedita
            accusamus fugiat ab hic. Inventore distinctio reprehenderit nisi,
            architecto tempora quibusdam mollitia itaque aperiam, explicabo
            atque, quasi dolores pariatur necessitatibus illo in enim autem
            accusantium. Quia, a quibusdam accusantium quo suscipit nulla,
            aspernatur rem dolor eaque placeat maxime excepturi dolorum, libero
            vel doloribus perferendis nobis in reprehenderit! Veniam harum, sed
            fugit adipisci minus non facere molestias! Nesciunt consequatur
            repellendus repellat alias dignissimos vero qui id iure ipsa
            distinctio eos, laborum ullam iste quisquam ab! Ad voluptatum quae
            ex voluptatem quasi totam at similique facilis a architecto animi
            facere molestias, expedita voluptas asperiores tempora sunt debitis
            quibusdam reiciendis! Sunt voluptatibus repellat sint vitae facilis,
            placeat optio, commodi nostrum dicta itaque sit nobis? Recusandae
            nobis deleniti facilis eos quos. Cumque similique provident dolorum
            molestias officiis nostrum id natus laboriosam autem laborum ipsam,
            hic maxime, rem quos sit pariatur placeat ipsum quod totam dolores?
            Similique unde ratione incidunt odit aliquid laudantium labore
            debitis earum! Tempora aliquam laboriosam magni ut placeat sint quam
            recusandae saepe? Labore eum, corrupti cum laboriosam rerum aperiam
            quos cumque qui temporibus autem possimus, enim id tempora
            exercitationem sunt ipsa officiis rem? Sint, voluptatum obcaecati
            amet cumque omnis tempore error repellat doloremque saepe, hic
            perferendis pariatur nemo dolor! Quod, modi? Nemo harum debitis
            fugit dolor quibusdam vero animi nulla est cumque cupiditate
            voluptatibus aliquid blanditiis, quam dolorum. Soluta nesciunt iste
            illum omnis maiores nisi veritatis consectetur, possimus modi libero
            architecto quidem tenetur repellendus, facilis perferendis
            perspiciatis atque tempora corrupti aut ipsam consequuntur deleniti
            animi asperiores molestiae! Harum illum qui debitis illo autem?
          </main>
        </div>
      </div>
    </Fragment>
  );
};
