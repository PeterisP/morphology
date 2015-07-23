/*******************************************************************************
 * Copyright 2008, 2009, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.semti.morphology.Testi;


import static org.junit.Assert.*;

import java.util.LinkedList;
import org.junit.BeforeClass;
import org.junit.Test;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;

public class TokenizeTest {
	private static Analyzer locītājs;
	//PrintWriter izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		locītājs = new Analyzer("dist/Lexicon.xml");
		//locītājs=null;
	}
	
	@Test
	public void cirvis() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "cirvis");
		assertEquals(1, tokens.size());
		assertEquals("cirvis", tokens.get(0).getToken());
	}	
	
	@Test
	public void kaķis() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "kaķis");
		assertEquals(1, tokens.size());
		assertEquals("kaķis", tokens.get(0).getToken());
	}
	
	@Test
	public void cirvis_āmurs() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "cirvis āmurs");
		assertEquals(2, tokens.size());
		assertEquals("āmurs", tokens.get(1).getToken());
		assertEquals("cirvis", tokens.get(0).getToken());
	}
	
	@Test
	public void cirvis_āmurs_galds() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "cirvis āmurs galds");
		assertEquals(3, tokens.size());
		assertEquals("āmurs", tokens.get(1).getToken());
		assertEquals("cirvis", tokens.get(0).getToken());
		assertEquals("galds", tokens.get(2).getToken());
	}
	
	@Test
	public void cirvis_punkts_āmurs() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "cirvis. āmurs");
		assertEquals(3, tokens.size());
		assertEquals("cirvis", tokens.get(0).getToken());
		assertEquals(".", tokens.get(1).getToken());
		assertEquals("āmurs", tokens.get(2).getToken());
	}
	
	@Test
	public void āmurs_jautZīmes() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "āmurs ?");
		assertEquals(2, tokens.size());
		assertEquals("āmurs", tokens.get(0).getToken());
		assertEquals("?", tokens.get(1).getToken());
	}
	
	@Test
	public void un_citi() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "kkas u.c. kkas");
		assertEquals(3, tokens.size());
		assertEquals("kkas", tokens.get(0).getToken());
		assertEquals("u.c.", tokens.get(1).getToken());
		assertEquals("kkas", tokens.get(2).getToken());
	}
	
	@Test
	public void un_citi2() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "u.c. kkas");
		assertEquals(2, tokens.size());
		assertEquals("u.c.", tokens.get(0).getToken());
		assertEquals("kkas", tokens.get(1).getToken());
	}
	
	@Test
	public void un_citi3() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "kkas u.c.");
		assertEquals(2, tokens.size());
		assertEquals("kkas", tokens.get(0).getToken());
		assertEquals("u.c.", tokens.get(1).getToken());
	}
	
	@Test
	public void un_citi4() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "u.c.");
		assertEquals(1, tokens.size());
		assertEquals("u.c.", tokens.get(0).getToken());
	}
	
	@Test
	public void unfinishedException()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "plk");
		assertEquals(1, tokens.size());
		assertEquals("plk", tokens.get(0).getToken());
	}
	
	@Test
	public void CaseTest()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "kaut arī KAUT ARĪ");
		assertEquals(2, tokens.size());
		assertEquals("KAUT ARĪ", tokens.get(1).getToken());
	}
	
	@Test
	public void iniciāļi()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "atnāca A. Bērziņš.");
		assertEquals(4, tokens.size());
		assertEquals("A.", tokens.get(1).getToken());
	}
	
	@Test
	public void datums()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "sapulce 2012. gada 3. aprīlī");
		assertEquals(5, tokens.size());
		assertEquals("2012.", tokens.get(1).getToken());
	}
	
	@Test
	public void pieturzīmes()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "Kas tas ?!?!?");
		assertEquals(3, tokens.size());
		assertEquals("?!?!?", tokens.get(2).getToken());
	}
	
	@Test
	public void pieturzīmes2()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "Kas tas⁈");
		assertEquals(3, tokens.size());
		assertEquals("⁈", tokens.get(2).getToken());
	}
	
	@Test
	public void pieturzīmes3()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "un tad-, kaut kas notika");
		assertEquals(7, tokens.size());
		assertEquals("-", tokens.get(2).getToken());
		assertEquals(",", tokens.get(3).getToken());
	}
	
	@Test
	public void pieturzīmes4()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "tiešā runa.\" teksts");
		assertEquals(5, tokens.size());
		assertEquals(".", tokens.get(2).getToken());
		assertEquals("\"", tokens.get(3).getToken());
	}
	
	@Test
	public void tilde()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "~vārds");
		assertEquals(2, tokens.size());
		assertEquals("~", tokens.get(0).getToken());
	}
	
	@Test
	public void ekomercija()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "e-komercija");
		assertEquals(1, tokens.size());
		assertEquals("e-komercija", tokens.get(0).getToken());

		tokens = Splitting.tokenize(locītājs, "e-komercijai");
		assertEquals(1, tokens.size());
		assertEquals("e-komercijai", tokens.get(0).getToken());
	}
	
	@Test
	public void apostrofs()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "Raug', Saule grimdama aicina mani,");
		assertEquals(7, tokens.size());
		assertEquals("Raug'", tokens.get(0).getToken());
		
		tokens = Splitting.tokenize(locītājs, "'desa'");
		assertEquals(3, tokens.size());
		assertEquals("desa", tokens.get(1).getToken());
	}
	
	@Test
	public void epasts()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "Pieteikumus sūtiet uz e-pastu vards.uzvards@domens.lv");
		assertEquals(5, tokens.size());
		assertEquals("vards.uzvards@domens.lv", tokens.get(4).getToken());
	}
	
	@Test
	public void saites()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "Failu var lejupielādēt http://www.faili.lv/fails.php?id=215");
		assertEquals(4, tokens.size());
		assertEquals("http://www.faili.lv/fails.php?id=215", tokens.get(3).getToken());
		
		tokens = Splitting.tokenize(locītājs, "Ftp adrese ftp://www.faili.lv/fails.php?id=215&actions=download");
		assertEquals(3, tokens.size());
		assertEquals("ftp://www.faili.lv/fails.php?id=215&actions=download", tokens.get(2).getToken());
		
		tokens = Splitting.tokenize(locītājs, "Mūsu mājas lapa www.skaistas-vietas.lv");
		assertEquals(4, tokens.size());
		assertEquals("www.skaistas-vietas.lv", tokens.get(3).getToken());
	}
	
	@Test
	public void skaitli()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "Nobalsoja 1/2 no balstiesīgajiem");
		assertEquals(4, tokens.size());
		assertEquals("1/2", tokens.get(1).getToken());
		
		tokens = Splitting.tokenize(locītājs, "Šobrīd tiešsaitē ir 12'456 lietotāji");
		assertEquals(5, tokens.size());
		assertEquals("12'456", tokens.get(3).getToken());
		
		tokens = Splitting.tokenize(locītājs, "Servera IP adrese ir 132.168.2.102");
		assertEquals(5, tokens.size());
		assertEquals("132.168.2.102", tokens.get(4).getToken());

	}
	
	@Test
	public void skaitļi2()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "Ls 5.- gadā");
		assertEquals(3, tokens.size());
		assertEquals("5.-", tokens.get(1).getToken());

		tokens = Splitting.tokenize(locītājs, "gadā Ls 5.-, pusgadā Ls 3,-");
		assertEquals(7, tokens.size());
		assertEquals("5.-", tokens.get(2).getToken());
		
		
		tokens = Splitting.tokenize(locītājs, "Ls 50.000,-");
		/*for (Word w : tokens) {
			System.out.println(w.getToken());
		}*/
		assertEquals(2, tokens.size());
		assertEquals("50.000,-", tokens.get(1).getToken());

		tokens = Splitting.tokenize(locītājs, "Cena Ls 0.40. Nākamais");
		assertEquals(5, tokens.size());
		assertEquals("Ls", tokens.get(1).getToken());
		assertEquals("0.40", tokens.get(2).getToken());

	}
	
	@Test
	public void skaitļi3()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "0.40.");
		assertEquals(2, tokens.size());
		assertEquals("0.40", tokens.get(0).getToken());
	}
	
	@Test
	public void noiepirkšanās()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "no iepirkšanās");
		assertEquals(2, tokens.size());
		assertEquals("iepirkšanās", tokens.get(1).getToken());
	}
	
	@Test
	public void džilindžers()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "Dž. Dz. Džilindžers.");
		assertEquals(4, tokens.size());
		assertEquals("Dz.", tokens.get(1).getToken());
	}
	
	@Test
	public void atstarpes()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "a t s t a r p e s");
		assertEquals(1, tokens.size());
		assertEquals("a t s t a r p e s", tokens.get(0).getToken());
	}
	
	@Test
	public void atstarpes2()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "te ir a t s t a r p e s");
		assertEquals(3, tokens.size());
		assertEquals("a t s t a r p e s", tokens.get(2).getToken());
	}
	
	@Test
	public void falseBruteSplit()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "te ir a t s t a r p e s",false);
		assertEquals(3, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, "kaut gan",false);
		assertEquals(1, tokens.size());
	}
	
	@Test
	public void BruteSplit()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "te ir a t s t a r p e s",true);
		assertEquals(11, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, "kaut gan",true);
		assertEquals(2, tokens.size());				
	}
	
	@Test
	public void NonLV()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "старик с топором");
		assertEquals(3, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, "München");
		assertEquals(1, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, "W. Şiliņs");
		assertEquals(2, tokens.size());		
	}
	
	@Test
	public void garbage()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "rop. KajiHHHH, MocKOBCKan o6\nacTb. Ha- 6epe*Ha« Cr. Pa3MHa, aom J* 17. Kay<5 HaUMeHbUlMHCTB. rop KMeB. yji. »IapKca, >i 3. KhcbckhA aaT. pa6. wy6 mm. Py,a3yTaKa.");
		assertTrue(tokens.size() > 0);
	}
	
	@Test
	public void spaces()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, " pee pilsata ar wiffu sirgu un mesumu  eelaususchi un eekrittuschi strahdneeki no ");
		assertEquals(12, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, " pee pilsata ar wiffu sirgu un mesumu  eelaususchi un eekrittuschi strahdneeki no ", true);
		assertEquals(12, tokens.size());
	}
	
	@Test
	public void vecadruka()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "şabeedrişka", false);
		assertEquals(1, tokens.size());
	}
	
	@Test
	public void ampersand()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "tom&jerry", false);
		assertEquals(3, tokens.size());

		tokens = Splitting.tokenize(locītājs, "cirvis&", false);
		assertEquals(2, tokens.size());
	}
	
	@Test
	public void A_Upitis()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "A. Upītis", false);
		assertEquals(2, tokens.size());

		tokens = Splitting.tokenize(locītājs, "A.Upītis", false);
		assertEquals(2, tokens.size());
	}
	
	@Test
	public void Klase()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "11.c", false);
		assertEquals(2, tokens.size());
	}
	
	@Test
	public void Laura10Aug()
	{
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "tikpat kā", false);
		assertEquals(1, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, "11:00", false);
		assertEquals(1, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, "11.00", false);
		assertEquals(1, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, "11.a", false);
		assertEquals(2, tokens.size());
	}

	@Test
	public void Pulkstenis()
	{
		LinkedList<Word> tokens;
		
		tokens = Splitting.tokenize(locītājs, "00:00", false);
		assertEquals(1, tokens.size());

		tokens = Splitting.tokenize(locītājs, "23:59", false);
		assertEquals(1, tokens.size());
		
		tokens = Splitting.tokenize(locītājs, "23:59:59", false);
		assertEquals(1, tokens.size());		

		//nekorekti formāti
		tokens = Splitting.tokenize(locītājs, "24:00", false);
		assertEquals(3, tokens.size());

		tokens = Splitting.tokenize(locītājs, "13:60", false);
		assertEquals(3, tokens.size());

		tokens = Splitting.tokenize(locītājs, "25:00", false);
		assertEquals(3, tokens.size());
	}
	
	@Test
	public void alacarte(){
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "a la");
		assertEquals(2, tokens.size());
		assertEquals("a", tokens.get(0).getToken());
		assertEquals("la", tokens.get(1).getToken());
		
		tokens = Splitting.tokenize(locītājs, "Lampiņa a la art deco ar plastmasas kupolu.");
		assertEquals(9, tokens.size());
		assertEquals("a", tokens.get(1).getToken());
		assertEquals("la", tokens.get(2).getToken());
		assertEquals("art", tokens.get(3).getToken());
	}
	
	@Test
	public void hm() {
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "H&M boss");
		assertEquals(4, tokens.size());
		assertEquals("M", tokens.get(2).getToken());
		assertEquals("boss", tokens.get(3).getToken());		
	}
	
	@Test
	public void laura20130129() {
		// bugreport, ka slikti strādā, ja ir newline tikai starp tokeniem
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, "\"Pillar\" dubulto pārdošanas apjomus\n2013.gada 28.janvāris.");
		assertEquals(11, tokens.size());
	}

	@Test
	public void datumi()
	{
		LinkedList<Word> tokens;
		
		tokens = Splitting.tokenize(locītājs, "2009-12-14", false); // ISO standarts
		assertEquals(1, tokens.size());

		tokens = Splitting.tokenize(locītājs, "2009.12.14", false); // Ar . kā seperatoru
		assertEquals(1, tokens.size());

		tokens = Splitting.tokenize(locītājs, "9999.99.99", false); // By design
		assertEquals(1, tokens.size());
	}
	
	@Test
	public void dzīvokļnumuri()
	{
		LinkedList<Word> tokens;
		
		tokens = Splitting.tokenize(locītājs, "16A", false); 
		assertEquals(1, tokens.size());
	}

	@Test
	public void apostrofpēdiņas()
	{
		LinkedList<Word> tokens;
		
		tokens = Splitting.tokenize(locītājs, "''", false); 
		assertEquals(1, tokens.size());
	}
	
	@Test
	public void ciparvirknes() {
		LinkedList<Word> tokens;
		
		tokens = Splitting.tokenize(locītājs, "87 89 90 93 95 98 501 3 12 13 16 26 32 33 35 43 44 52 60 72 88 90 92 609 16 21 22 26 28 36 37 40 43 44 47 51 53 56 64 71 73 88 92 94 96 97 98 99 703 9 11 19 21 25 26 29 31 32 35 55 56 58 60 61 67 69 73 76 78 81 83 86 807 14 15 20 22 27 38 39 40 46 58 63 65 69 70 72 86 90 912 17 18 19 21 22 24 42 51 52 56 57 63 64 68 69 71 75 80 85 91 93 51005 11 13 19 24 26 31 56 57 61 63 67 68 72 82 89 90 91 94 98 110 12 13 15 16 22 23 24 29 32 49 59 65 70 72 73 74 81 84 93 96 202 8 12 14 18 26 37 40 41 43 44 47 51 52 53 64 71 73 84 87 88 92 94 95 314 15 16 18 19 22 25 45 46 47 52 53 63 77 79 87 400 1 3 6 8 17 21 26 27 28 40 53 55 58 65 69 71 73 78 79 80 88 98 503 5 12 20 26 29 32 41 43 47 52 57 58 61 64 71 72 74 78 79 80 81 83 84 86 88 89 91 93 94 602 8 9 13 19 21 31 35 39 42 45 46 58 59 75 76 78 85 92 94 96 98 710 18 19 20 22 26 30 32 37 38 43 45 49 56 66 67 71 85 86 88 89 91 810 14 15 20 22 26 31 35 37 47 49 50 61 69 72 75 78 83 92 95 902 4 5 9 10 14 15 27 36 41 43 44 45 49 52 59 70 74 77 79 98 52001 3 10 13 15 16 20 22 27 45 48 51 52 55 59 62 63 67 70 80 83 87 91 95 100 2 3 5 7 16 20 24 32 40 42 49 54 57 59 64 65 71 76 79 82 89 91 99 202 6 10 13 15 16 17 18 30 36 38 39 50 54 65 66 68 71 72 76 79 86 89 95 300 1 4 6 8 13 17 20 22 25 28 30 37 45 50 60 67 72 77 83 89 93 404 6 11 14 19 24 25 31 33 35 42 45 48 60 70 71 75 77 79 86 506 11 12 14 15 17 19 23 27 28 30 35 44 47 50 65 70 71 75 78 82 90 91 95 96 99 601 4 14 18 45 55 58 69 75 89 91 99 701 5 11 21 23 24 37 40 45 46 59 61 63 67 69 70 84 85 90 94 96 99 800 1 2 3 8 34 38 40 46 48 67 69 70 83 86 89 90 92 903 5 10 15 17 19 20 29 35 37 40 47 52 53 58 63 65 71 72 74 75 80 85 93 97 53001 2 29 33 36 38 42 45 47 50 55 58 68 74 93 95 100 1 11 12 14 19 28 29 42 44 48 53 56 57 61 63 64 68 80 82 83 99 214 17 24 28 30 35 37 43 54 56 62 76 77 87 92 98 300 9 12 13 14 17 22 27 35 37 38 52 53 56 60 64 70 71 72 84 85 86 89 94 97 99 402 3 8 12 22 26 27 41 47 48 55 65 68 70 71 76 77 78 79 84 88 90 92 502 3 7 17 19 20 26 30 32 36 37 39 40 41 45 46 47 50 55 63 71 76 79 80 84 87 94 98 604 7 11 15 17 18 21 24 25 27 29 30 40 41 42 43 44 46 47 50 51 53 56 57 64 65 69 73 76 83 85 96 704 6 7 14 19 20 35 37 38 41 42 43 51 52 53 57 61 64 66 68 69 71 73 79 81 82 92 801 12 33 34 41 49 52 68 70 90 94 910 11 16 17 19 22 26 28 32 33 35 37 44 45 48 49 53 58 62 63 73 89 91 54000 2 13 16 19 27 28 34 37 43 45 47 54 60 65 84 90 97 110 17 31 49 50 62 63 79 90 91 93 205 11 16 28 29 30 31 32 38 52 58 75 85 89 96 97 303 7 8 10 18 26 37 41 42 47 51 53 56 57 58 63 69 71 73 75 76 79 82 85 91 96 97 98 99 419 20 23 28 32 38 42 46 51 59 64 67 73 77 85 86 95 500 5 12 13 18 19 22 28 41 42 45 50 54 58 69 70 81 85 90 92 98 611 14 16 25 26 31 38 44 46 47 52 74 76 81 83 84 90 701 9 10 11 20 24 26 30 36 45 49 52 54 62 65 67 75 93 99 808 9 11 15 17 18 24 28 30 35 37 39 46 48 56 62 64 65 68 72 75 80 85 87 92 93 94 98 902 4 9 13 15 24 26 27 29 37 45 46 47 58 59 60 68 69 71 79 85 87 89 91 95 55004 7 9 15 18 22 31 43 44.51 55 58 60 65 68 79 87 88 89 94 99 105 10 17 26 31 33 35 37 38 42 51 53 55 61 65 83 84 87 89 92 94 203 6 15 16 22 23 25 26 27 36 42 43 66 74 81 83 85 86 90 96 300 2 3 5 6 9 13 16 17 19 22 23 24 28 31 50 52 56 63 66 68 69 70 76 78 80 81 99 404 5 14 24 25 31 34 35 36 37 43 44 52 56 72 73 76 77 80 82 85 86 88 92 93 503 7 13 15 22 23 29 30 31 33 35 40 41 42 44 52 56 58 60 64 70 79 80 83 92 93 97 615 18 30 37 38 40 41 47 49 52 56 59 63 64 65 68 73 77 78 85 90 91 98 99 709 11 12 19 26 29 31 37 39 51 52 54 55 72 77 78 80 84 85 91 93 95 96 800 2 6 15 18 19 20 21 22 25 29 42 43 47 54 58 61 63 82 89 909 15 17 19 33 41 44 48 57 59 65 71 82 97 56005 7 11 30 33 36 39 46 48 49 53 67 79 89 91 92 95 100 6 8 20 37 46 52 56 62 63 71 72 97 99 212 13 19 21 47 48 50 52 53 57 84 85 88 92 300 2 7 11 15 21 36 39 41 50 55 59 66 72 94 96 400 1 3 6 7 13 14 17 21 37 39 43 51 53 59 63 65 73 74 75 76 78 95 505 8 14 18 19 20 25 40 46 47 55 57 58 59 66 67 72 81 83 86 95 96 601 4 7 12 25 32 44 54 56 58 61 63 70 73 74 75 76 79 80 92 701 4 6 14 19 31 32 39 40 41 49 52 53 58 68 76 81 87 98 801 11 13 31 60 63 75 77 94 99 900 4 28 29 38 39 40 42 47 59 63 66 70 74 77 87 91 92 95 99 57000 1 3 5 14 17 24 25 31 33 34 37 39 47 52 54 67 68 70 71 72 73 78 80 81 88 102 4 5 10 19 39 43 49 55 58 62 76 88 90 99 200 3 7 10 16 18 20 23 29 33 39 42 49 53 54 59 60 62 73 79 80 84 86 95 99 306 43 14 16 27 30 34 44 58 59 60 72 74 80 89 401 5 12 13 14 32 56 58 60 61 62 67 72 78 82 84 92 503 5 11 14 17 19 25 31 33 46 47 50 52 60 65 66 69 80 90 94 96 97 607 16 30 37 40 41 42 44 47 65 69 75 76 92 93 701 4 5 12 18 22 23 27 28 34 36 37 43 44 48 58 62 69 71 78 79 84 85 92 806 11 18 26 40 42 48 49 50 52 55 58 59 61 62 68 83 95 96 901 4 6 7 8 10 15 17 20 23 27 29 46 48 57 63 67 69 78 89 90 92 58003 9 11 16 17 19 20 25 29 34 38 42 51 55 61 63 82 83 88 94 105 12 19 21 34 36 38 45 49 56 57 60 63 67 68 73 87 205 11 14 17 19 20 25 43 55 66 70 72 77 86 88 91 98 307 23 29 35 41 43 44 50 52 53 60 61 65 66 69 75 82 87 89 402 10 16 17 19 23 25 27 31 36 38 44 46 52 "
				, false); 
		assertEquals(1, tokens.size());		
	}
	
	@Test
	public void sentences() {
		LinkedList<LinkedList<Word>> sentences;
		sentences = Splitting.tokenizeSentences(locītājs, "Vīrs ar cirvi.");
		assertEquals(1, sentences.size());
		
		sentences = Splitting.tokenizeSentences(locītājs, "Saule lec. Sniegs snieg.");
		assertEquals(2, sentences.size());
		
		sentences = Splitting.tokenizeSentences(locītājs, "Nosaukums: SIA \"Evopipes\"Nozares: cauruļu ražošanaAdrese: Langervaldes iela 2A, Jelgava, LV-3002, Latvija             Telefons: 63094300Internets: http://www.evopipes.lv <h1>Uzņēmuma apraksts</h1>2005.gadā tika dibināts uzņēmums SIA \"Modulex - Invest Jelgava\" ar  mērķi attīstīt cauruļu ražotnes projektu Latvijā, Jelgavā. 2008.gada  beigās uzņēmums tika pārdēvēts par SIA \"Evopipes\" ar mērķi zīmolu virzīt  starptautiskā tirgū. Uzņēmums sērijveidā ražo produkciju elektroinstalācijām,  elektrokabeļu aizsardzībai, telekomunikācijas kabeļu aizsardzībai,  aukstā ūdens apgādes sistēmu izveidošanai, apkures sistēmu izveidošanai,  gāzes apgādei, ēku kanalizācijai, sadzīves notekūdeņu kanalizācijai,  lietus notekūdeņu kanalizācijai, laukumu un ēku pamatu drenāžai.&nbsp;<h1>Finanšu rādītāji</h1><table border=\"0\"><tbody><tr><td> </td><td>2011 <br /></td><td>2010 <br /></td><td> </td><td> </td><td> </td></tr><tr><td> Apgrozījums (tūkst. Ls)<br /></td><td>9 370.000<br /></td><td>5 900.000<br /></td><td> </td><td> </td><td> </td></tr><tr><td>Peļņa/zaudējumi (tūkst. Ls) <br /></td><td> 532.468</td><td>- 1 300.000<br /></td><td> </td><td> </td><td> </td></tr></tbody></table><br /><br />");
		assertEquals(10, sentences.size());
		
		sentences = Splitting.tokenizeSentences(locītājs, "Valsts prezidents Guntis Ulmanis sniedzis interviju laikrakstam Diena, kurā paudis savu viedokli par nākamās valdības modeli, tās deklarācijas galvenajiem uzdevumiem, kā arī par Andra Šķēles personību. - Kopumā Ulmanis valdības deklarāciju novērtējis kā tādu, kurā tiek atmesti visi vājie punkti un ielikti pamatīgi, spēcīgi valdības darba principi. Tajā esot aptverti ne tikai makroekonomikas, bet arī struktūrpolitikas jautājumi, kurā būtiskākie esot bezdeficīta budżets, inflācijas ierobeżošana un kārtība, kā šis budżets tiks izmantots. Viņš pozitīvi novērtējis faktu, ka ir sākts domāt divu gadu kategorijās un izteica cerību, ka valdības būs tik stipras, ka nemainīsies vairs ik pēc gada, bet vismaz pēc diviem. Runājot par valdības vadītāju, Valsts prezidents izteicis vēlmi, lai tas nemainītos vismaz desmit gadus. Ulmanis apliecinājis, ka sarunās ar Ministru prezidentu Šķēli, konstatējis, ka pēdējais esot gatavs mainīt savu nostāju daudzos Latvijai būtiskos jautājumos, tai skaitā arī personiska rakstura jautājumos, piemēram, attiecību veidošanā ar partijām, ar parlamentu, arī \"attiecībās starp diviem prezidentiem\", kas līdz šim esot bijušas daudzmaz normālas. Taču Valsts prezidents atzinis, ka jaunās valdības darbs un viņa amatā pavadītais laiks esot sadūries, jo katrs esot gribējis pierādīt savu taisnību. Viņaprāt, pozitīvi esot tas, ka abas šīs institūcijas sākušas iedziļināties problēmās. Ulmanis paudis viedokli, ka šis Ministru prezidents esot izaudzis uz iepriekšējo premjeru darba bāzes un pieredzes - gan pozitīvās, gan negatīvās. Valsts prezidents paudis arī savu gandarījumu, ka valdības deklarācijā parādījušies morāles kritēriji. Raksturojot atsevišķus deklarācijas jautājumus, viņš par svarīgākajiem atzinis jautājumu par monopoliem, robeżkontroles jautājums, jautājumu par izglītības un zinātnes integrācijas reformu un nacionālās nozīmes centra izveidošanu, par vienu no galvenajām viņš minējis iedaļu par drošību un tieslietām, svarīga esot arī ārlietām veltītā nodaļa, kura ar laiku tiks uzlabota. Ulmanis apstiprinājis, ka teritoriālā reforma valstī esot jāveic, taču viņš nepiekrīt valsts ielikta rajona pārvaldnieka amatam, taču viss būšot skaidrs pēc pašvaldību vēlēšanām. - Valsts prezidents devis arī savu vērtējumu jaunajai valdībai, ko, viņaprāt, formāli nevar uzskatīt par stabilu, taču viņš izteicis pārliecību, ka nestabilitāte bija saistīta ar reorganizāciju, nevis ar reformu gaitu un valsts attīstību kopumā. Ulmanis paskaidrojis, ka Šķēle atvaļinājis finansu ministru Vasiliju Meļņiku aiz morāles principiem, kas tika uztverti daudz dziļāk, taču viņš atteicies komentēt DPS rīcības motīvus, izvirzot finansu ministra amatam Meļņika kandidatūru. Viņš apliecinājis, ka vēlēšanās reorganizēt valdību sakrita ar momenta aktualitāti un morāles jēdziena nozīmīgumu. Valsts prezidents paudis domu, ka demisija neesot traģēdija valstij, tās izraisītās pārmaiņas tikai varētu ienest MK daudz nopietnāku attieksmi. Viņš paskaidrojis, ka arī bez Meļņika morāles kritēriji esot figurējuši valdības reorganizācijas procesā. - Partijas, viņaprāt, rīkojoties saprātīgi un apdomīgi, tās daudz labāk zinot, ko tās grib panākt. Ulmanis arī paredzējis strīdus objektu ministru amatu sadalē, un tas būšot finansu ministrs. Viņš informējis par pastāvošo vienošanos, ka valdība netikšot sastādīta slepus no Valsts prezidenta. Uz jautājumu, vai Šķēlem izdosies sastādīt valdību, viņš apliecinājis, ka Saeimas ārkārtas vēlēšanas pagaidām neesot viņa darbu sarakstā, jo tas būtu nopietns satricinājums gan valstij, gan sabiedrībai. - Valsts Prezidents paudis savu viedokli arī attiecībā uz Latvijas iesaistīšanos ES, kā arī tās attiecībām ar kaimiņu valstīm, viņš izteicis arī savu atbalstu ĀM un tās vadītājam Valdim Birkavam (LC).");
//		for (LinkedList<Word> s : sentences) {
//			for (Word w : s) {
//				System.out.print(w.getToken()+' ');
//			}
//			System.out.println();
//		}
	}

	@Test
	public void nonbreakingspace() {
		LinkedList<Word> tokens;		
		tokens = Splitting.tokenize(locītājs, "a b\u00A0c", false);
		assertEquals(2, tokens.size());
	}
	
	@Test
	public void year_numerals() {
		LinkedList<Word> tokens;		
		tokens = Splitting.tokenize(locītājs, "1995.gads", false);
		assertEquals(2, tokens.size());
		assertTrue(tokens.get(0).isRecognized());	
		assertEquals(1, tokens.get(0).wordformsCount());
		assertEquals(AttributeNames.v_Ordinal, tokens.get(0).wordforms.get(0).getValue(AttributeNames.i_ResidualType)); // ordinal number
		assertEquals("xo", tokens.get(0).wordforms.get(0).getTag());
	}

	@Test
	public void singleletters() {
		LinkedList<Word> tokens;		
		tokens = Splitting.tokenize(locītājs, "bv  q  i", false);
		assertEquals(3, tokens.size());
	}
}
 
