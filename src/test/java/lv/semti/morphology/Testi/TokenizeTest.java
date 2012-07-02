/*******************************************************************************
 * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia; Author: Pēteris Paikens
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

public class TokenizeTest {
	private static Analyzer locītājs;
	//PrintWriter izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//locītājs = new Analyzer("dist/Lexicon.xml");
		locītājs=null;
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
		for (Word w : tokens) {
			System.out.println(w.getToken());
		}
		assertEquals(3, tokens.size());
		assertEquals("5.-", tokens.get(1).getToken());

		tokens = Splitting.tokenize(locītājs, "gadā Ls 5.-, pusgadā Ls 3,-");
		for (Word w : tokens) {
			System.out.println(w.getToken());
		}
		assertEquals(7, tokens.size());
		assertEquals("5.-", tokens.get(2).getToken());
		
		
		tokens = Splitting.tokenize(locītājs, "Ls 50.000,-");
		for (Word w : tokens) {
			System.out.println(w.getToken());
		}
		assertEquals(2, tokens.size());
		assertEquals("50.000,-", tokens.get(1).getToken());

		tokens = Splitting.tokenize(locītājs, "Cena Ls 0.40. Nākamais");
		for (Word w : tokens) {
			System.out.println(w.getToken());
		}
		assertEquals(5, tokens.size());
		assertEquals("Ls", tokens.get(1).getToken());
		assertEquals("0.40", tokens.get(2).getToken());

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
}
