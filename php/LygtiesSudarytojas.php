<?php
/**
 * Created by PhpStorm.
 * User: agnius
 * Date: 17.8.5
 * Time: 16.51
 */

class LygtiesSudarytojas {
    public $lygtis;
    public $zinute;
    public $klausiamiVienetai;

    protected function sp_01($rysiai) {

        if (count($rysiai) != 2)
            return [null, "ryšių kiekis nėra 2"];

        // ar matmenu tik po 2 ?
        foreach ($rysiai as $rysys) {
            if (count($rysys->arrMatmenys) != 2)
                return [null, "matmenų kiekis objekte nėra 2"];
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
            return [null, "Matmenys priskirti skirtingiems objektams"];

        // ar sutampa tipai ?
        for ($m=0; $m < 2; $m++) {
            $tipas1 = $rysiai[0]->arrMatmenys[$m]->tipas;
            $tipas2 = $rysiai[1]->arrMatmenys[$m]->tipas;
            if ($tipas1 != $tipas2)
                return [null, "Matmenų tipai skiriasi tarp objektų"];
        }

        return ["{$rysiai[0]->arrMatmenys[0]->kiekis} * {$rysiai[1]->arrMatmenys[1]->kiekis} = {$rysiai[0]->arrMatmenys[1]->kiekis} * {$rysiai[1]->arrMatmenys[0]->kiekis}", "ok"];
    }

    protected function sp_02($rysiai) {
        return [null, "Nesuprogramuota"];
    }

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

        // ieškome atsakymo
        $methods = get_class_methods('LygtiesSudarytojas');

        foreach ($methods as $method) {
            if (preg_match('/^sp_/mui', $method)) {
                list($lygtis, $statusas) = $this->$method($rysiai);
                $this->lygtis = $lygtis;
                $this->zinute .= ($this->zinute ? "<br>" : "") . $method . " : " . $statusas;
                if ($lygtis)
                    break;
            }
        }

    }
}
