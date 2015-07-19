import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

public class AlexaParser {

	public static void main(String[] args) {
		int fail = 0;
		int success = 1;
		for (String website : websites) {
			Map<String, Double> result = parseAlexa(website);
			if (result == null)
				fail++;
			else
				success++;
			System.out.println(success + ":" + fail + " - " + result);
		}
	}

	public static Map<String, Double> parseAlexa(String website) {
		// turn off output of irrelevant information
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.WARNING);

		WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false); // faster execution, less errors
		webClient.getOptions().setCssEnabled(false);

		HtmlPage currentPage;
		try {
			currentPage = webClient.getPage("http://www.alexa.com/siteinfo/" + website);
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
			return null;
		}

		HtmlTable countryTable = currentPage.getHtmlElementById("demographics_div_country_table"); // parse table
		String cssClass = countryTable.getAttribute("class");

		if (!cssClass.contains("data-table-nodata")) { // check if country data is available
			DomNodeList<HtmlElement> countryTableRows = countryTable.getHtmlElementsByTagName("tbody").get(0)
					.getElementsByTagName("tr"); // parse table rows

			Map<String, Double> tableData = new HashMap<String, Double>();

			for (HtmlElement line : countryTableRows) {
				DomNodeList<HtmlElement> cells = line.getElementsByTagName("td"); // parse row cells
				// remove the % sign from the second cell's content and convert to double
				Double percentage = Double.parseDouble(cells.get(1).asText().replace("%", "").trim());
				tableData.put(cells.get(0).asText().trim(), percentage); // add to output map
			}
			return tableData;
		}
		return null;
	}

	static String[] websites = { "http://www.airbnb.com", "http://www.vrbo.com", "http://www.homeaway.com",
			"http://www.uber.com", "http://www.couchsurfing.org", "http://www.blablacar.de", "https://www.flipkey.com",
			"http://www.freewheelers.co.uk", "http://www.homeaway.co.uk", "https://liftshare.com",
			"http://www.holidaylettings.co.uk", "http://www.spareroom.co.uk", "http://www.ownersdirect.co.uk",
			"http://www.housetrip.com", "https://www.rci.com", "http://www.bnbhero.com", "http://corp.fon.com",
			"http://www.niumba.com", "http://roomorama.com", "http://www.wimdu.com ", "http://uk.travelmob.com",
			"http://www.stayz.com.au", "http://www.relayrides.com", "http://www.9flats.com",
			"http://www.holidayrentals.com", "http://www.erento.com", "http://onefinestay.com",
			"http://www.vacationhomerentals.com", "http://www.appartager.com", "http://kozaza.com",
			"http://www.homeexchange.com", "http://www.homestay.com", "http://m.tujia.com",
			"http://www.thestorefront.com", "http://www.sparefoot.com", "http://www.drivy.com", "http://getaround.com",
			"http://www.citiesreference.com", "http://3dhubs.com", "http://zilok.com", "http://liquidspace.com",
			"http://www.bedycasa.com", "http://www.miet24.de", "http://www.ridematch.511.org",
			"http://hemenkiralik.com", "http://www.eatwith.com", "http://www.amovens.com", "http://us.amovens.com",
			"http://www.guesttoguest.com", "https://boatbound.co", "http://www.lovehomeswap.com",
			"http://www.spothero.com", "http://www.yolyola.com", "http://www.streetbank.com", "http://www.halldis.com",
			"http://www.homeswapper.co.uk", "http://www.ownerdirect.com", "http://www.side.cr",
			"http://www.parkingpanda.com", "http://hailocab.com", "https://www.justpark.com", "http://www.uguest.com",
			"http://www.misterbnb.com", "http://www.incrediblue.com", "http://Warmshowers.org",
			"http://www.zaranga.com", "http://breather.com", "http://snappcar.nl", "http://www.borrowmydoggy.com",
			"http://www.iha.com", "https://zilyo.com", "http://www.flat-club.com", "https://www.roomertravel.com",
			"http://www.movebubble.com", "https://flightcar.com", "http://www.zimride.com", "http://www.meemeep.com",
			"http://sabbaticalhomes.com", "http://www.waytostay.com", "http://usselfstoragelocator.com",
			"http://gomore.dk", "http://www.kitchit.com", "http://www.apartmentsapart.com",
			"https://www.airvy-locationdecampingcar.com", "http://hirespace.com", "http://www.carpoolworld.com",
			"http://www.carpooling.com", "http://rentalo.com", "http://www.knok.com", "http://www.monsieurparking.com",
			"http://www.autonetzer.de", "http://cabify.com", "http://www.yachtico.com",
			"http://www.gamedayhousing.com", "http://www.wundercar.org", "http://Www.trocmaison.com",
			"http://www.eatfeastly.com", "http://www.homeforexchange.com", "http://www.makexyz.com",
			"http://hackerspaces.org", "http://www.sejourning.com", "http://www.vadrouille-covoiturage.com",
			"http://www.vacationrentalpeople.com", "http://www.desktimeapp.com", "https://www.spinlister.com",
			"http://www.sailsquare.com", "http://www.buzzcar.com", "http://en.homeforhome.com",
			"http://www.socialcar.com", "https://londonluton.liftshare.com", "http://www.carnextdoor.com.au",
			"http://www.parkatmyhouse.com", "http://www.theotherhome.com", "http://djump.in", "http://ridejoy.com",
			"http://www.tamyca.de", "http://peerby.com", "https://alterkeys.com", "https://www.e-loue.com",
			"http://www.openplay.co.uk", "http://www.drivemycarrentals.com.au", "http://www.stopsleepgo.com",
			"http://www.nomador.com", "https://trampolinn.com", "http://www.impossible.com",
			"http://wadeshealy.3rdhome.com", "http://gb.intervac-homeexchange.com", "http://www.costockage.fr",
			"http://www.ripenear.me", "http://www.unicaronas.com.br", "http://getmyboat.com", "http://gocarshare.com",
			"https://www.jelouemoncampingcar.com", "http://www.ridingo.com", "http://www.justshareit.com",
			"https://storemates.co.uk", "http://www.my-apartment-in-paris.com", "http://www.interhome.co.uk",
			"http://www.roadsharing.com", "http://flinc.org", "http://www.crashmypad.com",
			"http://www.citizenshipper.com", "https://www.homestaybrazil.com.br", "https://www.cosmopolithome.com",
			"http://www.deways.com", "http://www.erideshare.com", "http://coworkingproject.com",
			"http://www.lamachineduvoisin.fr", "http://campinmygarden.com", "http://www.irentshare.com",
			"http://www.BookLending.com", "http://www.leihdirwas.de", "http://www.123envoiture.com",
			"http://www.icarpool.com", "http://www.guardianhomeexchange.co.uk", "http://www.gottapark.com",
			"http://www.kiraguru.com", "http://www.yourparkingspace.co.uk", "http://www.jayride.com.au",
			"http://www.rideshareonline.com", "http://www.parkingmadeeasy.com.au", "https://plateculture.com",
			"http://www.canubring.com", "http://www.mytwinplace.com", "https://worldcraze.com",
			"http://www.rentmyitems.com", "https://sharoo.com", "http://tryloveroom.com",
			"http://www.covoiturage-dynamique.eu", "https://minbildinbil.dk", "https://www.cameralends.com",
			"http://www.ridester.com", "http://www.private-homes.com", "http://www.gay-ville.com",
			"http://www.geronimo.com", "http://neighborhoodfruit.com", "https://fun2rent.com", "http://desksnear.me",
			"http://storenextdoor.com", "http://carmanation.com", "http://www.livop.fr", "http://www.rentecarlo.com",
			"http://www.homebase-hols.com", "http://divvy.com.au", "http://www.piggybee.com",
			"http://www.findacarpark.com.au", "http://openmarq.com", "http://www.geenee.com",
			"http://www.spaceout.com.au", "http://www.hirejungle.co.uk", "http://www.hitchhikers.org",
			"http://instantcab.com", "http://www.sincropool.com", "http://www.parkonmydrive.com",
			"http://www.superfred.it", "http://www.boatsetter.com", "http://www.toogethr.com",
			"http://www.gearcommons.com", "http://DriveHubber.com", "http://www.caronetas.com.br",
			"http://www.magicevent.com", "http://intersailclub.com", "http://www.shareyourride.net",
			"http://www.ridebuzz.org", "http://casaversa.com", "http://www.nachbarschaftsauto.de",
			"http://embassynetwork.com", "http://www.parkcirca.com", "http://www.ParkingCarma.com",
			"http://www.exchangezones.com", "http://www.rentoid.com", "http://gloveler.com",
			"http://www.landshare.net", "http://www.ivhe.com", "http://parkhound.com.au",
			"http://communitygarden.org.au", "http://www.easynest.com", "http://www.frents.com",
			"http://www.carpoolarabia.com", "http://rentmama.com", "http://www.venues.org.uk", "http://www.skylib.com",
			"http://www.openshed.com.au", "http://sharely.us", "http://www.karzoo.eu", "http://www.meerijden.nu",
			"http://www.sabbatix.com", "http://carshare.org", "http://www.hotdesk.com.au",
			"http://www.surfbreakrentals.com", "https://www.rentything.com", "http://www.getdigsy.com",
			"http://www.private-holiday.com", "http://www.broadsabroad.net", "http://www.ideophone.in",
			"http://1000tools.com", "http://www.bnbboat.com", "http://peerspaceapp.com", "http://parkmyvan.com.au",
			"https://www.dreamflat.co.uk", "http://athomeinfrance.com", "http://www.campaya.co.uk",
			"http://www.safelystay.com", "http://www.sharemystorage.com", "http://www.aussiehouseswap.com.au",
			"http://www.ahparis.com", "http://spacefinity.com", "http://www.kookopa.com",
			"http://www.huertoscompartidos.com", "https://app.sharehammer.com", "http://www.smoovup.com",
			"http://www.coseats.com", "http://www.homeexchange50plus.com", "http://www.alkiloo.com",
			"http://www.jumpinstudent.co.uk", "http://www.rentcharlie.com", "http://www.paris-sharing.com",
			"http://www.bedandfed.co.uk", "http://go2gether.ca", "http://www.simplecharters.com",
			"http://www.houseswapholidays.com.au", "http://www.storeatmyhouse.com", "http://fun2boat.com",
			"http://lenderise.com", "http://www.echangeimmo.com", "http://pendlernetz.de", "http://www.qraft.com",
			"https://www.usetwice.at", "https://www.bookelo.com", "http://fanbed.com", "http://www.gweet.com",
			"http://sharemyfare.com", "http://www.catchalift.com", "http://samenrijden.nl", "http://www.digsville.com",
			"http://www.sharingbackyards.com", "http://www.greenriders.fi", "http://Www.homelink.fr",
			"http://www.spareground.co.uk", "http://www.datemywardrobe.com", "http://rideshare.org",
			"http://myridebuddy.com", "http://www.urbangardenshare.org", "http://www.travelhomeexchange.com",
			"http://www.popupbrands.com.au", "http://www.carsharing.ie", "http://www.sharedearth.com",
			"http://www.swapeo.com", "http://www.carpoolone.com.au", "http://www.gti-home-exchange.com",
			"http://www.exchangeaway.com", "http://www.stowthat.com", "http://www.homeforswap.com",
			"http://tooxme.com", "http://www.rentaurus.com", "http://www.invented-city.com", "http://suitematch.com",
			"http://unstash.com", "http://anyhire.com", "http://www.singleshomeexchange.com",
			"http://www.givengine.org", "http://jipio.com", "http://es.makoondi.com",
			"http://www.thevacationexchange.com", "http://carambla.com", "http://fr.cityzencar.com",
			"http://www.backseatsurfing.com", "http://www.paris-be-a-part-of-it.com", "http://WeekendHouseSwap.com",
			"http://www.cabcorner.com", "http://www.asparetoshare.com", "https://www.lyft.com",
			"http://www.storeitsquirrel.com", "http://cabpal.co.uk", "http://www.fieldlover.com",
			"http://www.seemysea.com", "http://www.velogistics.net", "http://volo.io", "http://www.wheelz.com",
			"http://www.villefluide.fr", "http://www.sosimpleholidayswaps.com", "http://freegler.com",
			"http://gishigo.com", "http://www.shareling.com", "http://www.swapmycitypad.com",
			"http://www.anyfriendofours.com", "http://www.swapyourshop.com", "http://www.taxi.to",
			"http://www.rentamus.com", "http://www.wepatch.org", "https://www.twogo.com", "http://actsofsharing.com",
			"https://propaloo.com", "http://kartag.com", "http://www.gearspoke.com", "http://streetlend.com",
			"http://www.1sthomeexchange.com", "http://aggregatend.com", "http://www.awaynshare.com",
			"http://backcountryrides.com", "http://www.blablacar.com", "http://www.boatswap.info",
			"http://cabeasy.com", "http://www.caronasolidaria.com.br", "http://carpoolking.com.au",
			"http://www.carpoolplaza.be", "http://www.carsurfing.com", "http://www.carticipate.com",
			"http://www.comborides.com", "http://compartir.org", "http://www.coopiloto.net",
			"http://coworkingsingapore.com", "http://cvtc-nh.org", "http://www.flipshelf.com",
			"http://www.homeexchangeplace.com", "http://www.homewelcome.com", "http://www.intervacus.com",
			"http://www.i-shareit.com", "http://www.istopover.com", "http://www.itamos.com",
			"http://www.liftsurfer.com", "http://www.literatoo.com", "http://www.liverightsize.com",
			"http://www.luxehomeswap.com", "http://www.niriu.com", "http://www.rentandshare.co",
			"http://www.rentingpoint.com", "http://www.rentkart.com", "http://www.rideshare.co.uk",
			"http://www.share4friends.com", "http://www.sharendo.com", "http://shareshed.ca",
			"http://www.simplyhomeexchange.com", "http://switchhomes.net", "http://www.yourhomeformine.com",
			"http://www.alwaysonvacation.com", "http://www.redawning.com", "http://www.staydu.com",
			"http://de.travelmob.com", "https://www.dwellable.com", "http://www.i-likelocal.com",
			"https://www.theroomlink.co.za", "http://hovelstay.com", "https://www.e-domizil.de", "http://temptoy.com",
			"https://www.cruzin.com", "http://www.boatbay.com", "http://www.weareonaboat.com",
			"http://www.bookcrossing.com", "https://rvwithme.com", "http://rvshare.com", "http://www.icarsclub.com",
			"http://bj.ppzuche.com", "https://carclub.easycar.com", "http://www.samferda.net",
			"http://www.kangaride.com", "https://carmacarpool.com", "http://letsride.in", "http://www.tripda.com",
			"https://liftshare.com", "http://www.joinuptaxi.com", "http://bandwagon.io", "http://travelingspoon.com",
			"http://www.kitchensurfing.com", "https://www.cookening.com", "https://www.mealsharing.com",
			"http://bookalokal.com", "http://meetmeals.com", "http://www.bonappetour.com", "https://www.cozymeal.com",
			"http://www.laruchequiditoui.fr", "http://signup.zigair.com", "https://airpooler.com",
			"https://flytenow.com", "https://www.cojetage.com", "http://whyownit.com", "http://packmule.it",
			"http://zaagel.com", "https://www.cabenamala.com.br", "http://shipeer.com", "https://openwireless.org" };

}
