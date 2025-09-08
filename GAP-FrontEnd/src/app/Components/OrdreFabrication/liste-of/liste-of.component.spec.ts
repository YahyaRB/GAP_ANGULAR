import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeOFComponent } from './liste-of.component';

describe('ListeOFComponent', () => {
  let component: ListeOFComponent;
  let fixture: ComponentFixture<ListeOFComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListeOFComponent]
    });
    fixture = TestBed.createComponent(ListeOFComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
