<?php
/**
 * Created by PhpStorm.
 * User: agnius
 * Date: 17.8.5
 * Time: 16.45
 */

include_once "Pagalbiniai.php";

class Parseris {
    // gramatika
    const sakiniuSkirtukuPaieska = '/[.!]+/mui';
    const daiktavardziuPaieska = '/(?:[^a-ž]|^)(Marytė)(?:[^a-ž]|$)/mui';
    const klaustukoPaieska = '/\?+\s*$/mui';

    // matmenys
    const matmenysLaikas = '/(\d+(?:\.\d+)?)\s+(valanda|valandas|val|min)/mui';
    const matmenysAtstumas = '/(\d+(?:\.\d+)?)\s+(kilometrai|kilometrų|km)/mui';

    // klausiami matmenys
    const klausiamasAtstumas = '/Kiek\s+(kilometrų|km|metrų)/mui';

    // vienetu tumpinimas i trumpini
    const vienetaiItrumpini = ['/(kilometr|km)/mui' => 'km'];

    public $sakiniai = [];

    protected $lastObjectId = 0;

    protected function rastiDaiktavardzius($eilute, &$arrObjektai)
    {

        preg_match_all(self::daiktavardziuPaieska, $eilute, $daiktavardziai, PREG_SET_ORDER, 0);

        foreach ($daiktavardziai as $daiktavardis) {
            $objId = null;

            // ieskom gal toks objektas jau yra aprasytas ?

            foreach ($this->sakiniai as $s) {
                foreach ($s->arrObjektai as $obj) {
                    if (preg_match("/^{$daiktavardis[1]}$/mui", $obj->pavadinimas)) {
                        $objId = $obj->id;
                        goto SKIP_OBJ_SEARCH;
                    }
                }
            }
            SKIP_OBJ_SEARCH:

            $objId === null && $objId = ++$this->lastObjectId;

            $o = new Objektas();
            $o->pavadinimas = $daiktavardis[1];
            $o->id = $objId;

            $arrObjektai[] = $o;
        }
    }

    protected function rastiMatmenis($eilute, &$arrMatmenys) {
        // ieskom laiko
        preg_match_all(self::matmenysLaikas, $eilute, $laikai, PREG_SET_ORDER, 0);

        foreach ($laikai as $laikas) {
            $matmuo = new Matmuo();
            $matmuo->kiekis = $laikas[1];
            $matmuo->tipas = 'LAIKAS';
            $matmuo->vienetai = $laikas[2];

            $arrMatmenys[] = $matmuo;
        }

        // ieskom atstumu
        preg_match_all(self::matmenysAtstumas, $eilute, $atstumai, PREG_SET_ORDER, 0);

        foreach ($atstumai as $atstumas) {
            $matmuo = new Matmuo();
            $matmuo->kiekis = $atstumas[1];
            $matmuo->tipas = 'ATSTUMAS';
            $matmuo->vienetai = $atstumas[2];

            $arrMatmenys[] = $matmuo;
        }

    }

    function __construct($uzdavinys)
    {

        $tekstas = preg_split(self::sakiniuSkirtukuPaieska, $uzdavinys);

        foreach ($tekstas as $eilute) {
            if (strlen($eilute) == 0)
                continue;

            $sakinys = new Sakinys();
            $sakinys->eilute = $eilute;

            // kokia idomi info sakiniuose
            $this->rastiDaiktavardzius($eilute, $sakinys->arrObjektai);
            $this->rastiMatmenis($eilute, $sakinys->arrMatmenys);

            //  ar klausiamasis
            $arKlausiamasis = (bool) preg_match(self::klaustukoPaieska, $eilute);
            if ($arKlausiamasis) {
                // ziurime ko klausia
                preg_match_all(self::klausiamasAtstumas, $eilute, $klausiaAtstumo, PREG_SET_ORDER, 0);
                if ($klausiaAtstumo) {
                    $matmuo = new Matmuo();
                    $matmuo->kiekis = 'x';
                    $matmuo->tipas = 'ATSTUMAS';
                    $matmuo->vienetai = $klausiaAtstumo[0][1];

                    // nustatome vienetu trumpini
                    $vnt = 'vnt';
                    foreach (self::vienetaiItrumpini as $vienetas => $trumpas) {
                        if (preg_match($vienetas, $matmuo->vienetai)) {
                            $vnt = $trumpas;
                            break;
                        }
                    }
                    $matmuo->vienetaiTrumpas = $vnt;

                    $sakinys->arrMatmenys[] = $matmuo;
                }
            }

            $this->sakiniai[] = $sakinys;
        }

    }

}