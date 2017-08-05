<?php
/**
 * Created by PhpStorm.
 * User: agnius
 * Date: 17.8.5
 * Time: 16.40
 */

class Objektas {
    public $pavadinimas;
    public $id;
    public $arrMatmenys = [];
}

class Matmuo {
    public $kiekis;
    public $tipas;
    public $vienetai;
    public $vienetaiTrumpas;
}

class Sakinys {
    public $eilute;
    public $arrObjektai = [];
    public $arrMatmenys = [];
}
