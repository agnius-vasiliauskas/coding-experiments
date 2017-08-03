<?php

class Objektas {
    public $pavadinimas;
    public $id;
    public $arrMatmenys;
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

class MatmenuPriskyrejas {
    public $rysiai = [];

    function __construct($isparsintiSakiniai)
    {
        foreach ($isparsintiSakiniai as $sakinys) {
            if (count($sakinys->arrObjektai) == 0)
                continue;

            $objRysiai = null;

            foreach ($sakinys->arrMatmenys as $matmuo) {
                // susigeneruojame objekta kam priskirsim matmenis
                if (count($sakinys->arrObjektai) == 1) {
                    $objRef = $sakinys->arrObjektai[0];
                    if ($objRysiai === null) {
                        $objRysiai = new Objektas();
                        $objRysiai->id = $objRef->id;
                        $objRysiai->pavadinimas = $objRef->pavadinimas;
                    }
                }
                else
                    throw new Exception("Nemoku išspręsti uždavinio");

                $objRysiai->arrMatmenys[] = $matmuo;
            }

            if ($objRysiai) {
                $this->rysiai[] = $objRysiai;
            }
        }
    }
}

class LygtiesSudarytojas {
    public $sprendimoTipas;
    public $lygtis;
    public $klausiamiVienetai;

    function __construct($rysiai)
    {
        // nustatome atsakymo vienetus
        foreach ($rysiai as $rysys) {
            foreach ($rysys->arrMatmenys as $matmuo) {
                if (!preg_match('/\d+(?:\.\d+)?/mui', $matmuo->kiekis)) {
                    $this->klausiamiVienetai = $matmuo->vienetaiTrumpas;
                    goto TOLIAU;
                }
            }
        }

        TOLIAU:

        if (count($rysiai) != 2)
            throw new Exception("Nemoku išspręsti uždavinio");

        // ar matmenu tik po 2 ?
        foreach ($rysiai as $rysys) {
            if (count($rysys->arrMatmenys) != 2)
                throw new Exception("Nemoku išspręsti uždavinio");
        }

        // parusiuojame objekto matmenis pagal tipa
        foreach ($rysiai as $rysys) {
            usort($rysys->arrMatmenys,
                function ($m1, $m2) {
                    if ($m1->tipas == $m2->tipas)
                        return 0;
                    else
                        return ($m1->tipas < $m2->tipas) ? -1 : +1;
                }
            );
        }

        // ar matmenys priskirti tam paciam objektui ?
        $id1 = $rysiai[0]->id;
        $id2 = $rysiai[1]->id;

        if ($id1 != $id2)
            throw new Exception("Nemoku išspręsti uždavinio");

        // ar sutampa tipai ?
        for ($m=0; $m < 2; $m++) {
            $tipas1 = $rysiai[0]->arrMatmenys[$m]->tipas;
            $tipas2 = $rysiai[1]->arrMatmenys[$m]->tipas;
            if ($tipas1 != $tipas2)
                throw new Exception("Nemoku išspręsti uždavinio");
        }

        $this->sprendimoTipas = 'ANALOGIJA';

        $this->lygtis = "{$rysiai[0]->arrMatmenys[0]->kiekis} * {$rysiai[1]->arrMatmenys[1]->kiekis} = {$rysiai[0]->arrMatmenys[1]->kiekis} * {$rysiai[1]->arrMatmenys[0]->kiekis}";
    }
}

class Sprendikas {

    public $atsakymas;

    function __construct($lygtis)
    {
        $cmd = "maxima --very-quiet -r \"ratprint: false$ numer:true$ solve ([{$lygtis}], [x]);\"";
        $out = shell_exec($cmd);
        preg_match_all('/\[\w\s+=\s+(\d+(?:\.\d+)?)\]/mui', $out, $ats, PREG_SET_ORDER, 0);
        $this->atsakymas = $ats[0][1];
    }
}

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


?>

