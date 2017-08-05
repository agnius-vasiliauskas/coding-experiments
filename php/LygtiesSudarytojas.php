<?php
/**
 * Created by PhpStorm.
 * User: agnius
 * Date: 17.8.5
 * Time: 16.51
 */

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
