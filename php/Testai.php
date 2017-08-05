<?php
/**
 * Created by PhpStorm.
 * User: agnius
 * Date: 17.8.5
 * Time: 16.54
 */

include_once "Parseris.php";
include_once "MatmenuPriskyrejas.php";
include_once "LygtiesSudarytojas.php";
include_once "Sprendikas.php";

$uzdaviniai = ["Marytė per 2 valandas dviračiu nuvažiuoja 15 kilometrų. Kiek kilometrų Marytė nuvažiuos per 3 valandas ?",
               "Jonukas 3 metais vyresnis už Antaną. Antanas 5 metais vyresnis už Onutę. Keliais metais Jonukas vyresnis už Onutę ?"];

foreach ($uzdaviniai as $salyga) {

    echo '<pre>';

    print_r($salyga);

    try {
        $parseris = new Parseris($salyga);
        $matmenys = new MatmenuPriskyrejas($parseris->sakiniai);
        $lygtis = new LygtiesSudarytojas($matmenys->rysiai);
        $sprendikas = new Sprendikas($lygtis->lygtis);

        print_r("<p style='color: blue'>{$sprendikas->atsakymas} {$lygtis->klausiamiVienetai}</p><hr>");

    }
    catch (Exception $e) {
        print_r("<p style='color: red'>{$e->getMessage()}</p><hr>");
    }

    echo '</pre>';

}
